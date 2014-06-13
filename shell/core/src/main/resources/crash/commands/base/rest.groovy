import org.crsh.shell.ui.UIBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import org.crsh.jcr.command.ContainerOpt;
import org.crsh.jcr.command.UserNameOpt;
import org.crsh.jcr.command.PasswordOpt

import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Option;

import org.crsh.cmdline.spi.Value
import org.crsh.cmdline.rest.RestRead;

public class rest extends org.crsh.command.CRaSHCommand {

  @Usage("Login on the rest server.")
  @Man("The cyntax: rest login -u {user} -p {pass} ")
  @Command
  public Object login(
    @UserNameOpt String userName,
    @PasswordOpt String password) {
    
    def builder = new UIBuilder();
    userInfo = "";
    if(userName != null && password != null) {
      userInfo = userName + ":" + password;
    } else {
      userInfo = "root:gtn";  
    }
    
    System.getProperties().put("user", userInfo);
    builder.node("Info: {userName: " + userName + ", password: " + password +"}\n");

    RestRead read = new RestRead(domain + "/" + restName +"/private/jcr/repository/portal-system", userInfo, "GET");
    def result = read.getData();
    builder.node(((result == null || result.trim().length() == 0) ? "Login failed for user: " :  "Login successully for user: ")  + userInfo);
      
    return builder;
  }

  @Usage("Init information of server.")
  @Man("The cyntax: domain=http://localhost:8080;rest=rest;user=root:gtn;version=v1")
  @Command
  public void use(
    @Argument(unquote = false)
    @Usage("""\

    domain: The domain of rest sevice (by default is domain=http://localhost:8080)
    user: The information of current user (by default is user=root:gtn)
    rest: The rest name (by default is rest=rest)
    useVersion: The version of rest url (by default is false)
    """)
    @Man("Input the information of rest system") Value.Properties parameters) {
    java.util.Properties props = new java.util.Properties();
    java.util.Properties sysProps = System.getProperties();
    try {
      if(parameters != null && parameters.getProperties() != null) {
        props = parameters.getProperties();
      }
    } catch (Exception e) {}
    domain = props.get("domain", sysProps.get("domain", ""));
    if(domain.length() == 0) {
      domain = "http://localhost:8080";
    }
    restName = props.get("rest", sysProps.get("rest", ""));
    if(restName.length() == 0) {
      restName = "rest";
    }
    restVersion = "";
    useVersion = props.get("useVersion", sysProps.get("useVersion", "false"));
    if("true".equalsIgnoreCase(useVersion)) {
      RestRead read = new RestRead(domain + "/" + restName +"/api/social/version/latest.json", "", "GET");
      restVersion = read.getData().replace("{\"version\":\"", "").replace("\"}", "").trim();
    }
    //
    sysProps.put("domain", domain);
    sysProps.put("rest", restName);
    sysProps.put("version", restVersion);
    sysProps.put("like", "method=PUT;action=like");
    sysProps.put("rm", "method=DELETE");
    sysProps.put("add", "method=POST");
    sysProps.put("update", "method=POST");
    sysProps.put("comments", "method=POST");
  }
  
  
  @Usage("Get the data of rest url provide by eXoPlatform.")
  @Man("String path: the path url ex: rest/private/api/social/v1-alpha3/portal/feed.json")
  @Command
  public Object get(
    @Argument(unquote = false)
    @Usage("The path url rest ex: social/activities")
    @Man("Input the path of rest url")
    String path,
    @Argument(unquote = true)
    @Usage("Input the query parameters [ex: limit=10;offset=0;method=get]")
    @Man("The parameters of rest") Value.Properties parameters) {
    java.util.Properties props = new java.util.Properties();
    try {
      if(parameters != null && parameters.getProperties() != null) {
        props = parameters.getProperties();
        System.out.println("parameters " + parameters.getString());
      }
    } catch (Exception e) {}
    //
    return get_(path, props);
  }
  
  private Object get_(String path, java.util.Properties props) {
    def builder = new UIBuilder();
    if(domain == null) {
      throw new ScriptException("Need use command: 'rest use' before use this command");
    }
    if(userInfo == null) {
      throw new ScriptException("Need use command: 'rest login' to login user before use this command");
    }
    if(path == null) {
      throw new ScriptException("Need input the path url to get data of rest.");
    }
    path = domain + "/" + restName + ((restVersion.length() > 0) ? ("/" + restVersion) : "") + "/" + path;
    builder.node("Info: {restURL : " + path + ", user: " + userInfo +"}");
    
    Enumeration<Object> e = props.keys();
    def params = "", method = "GET";
    while (e.hasMoreElements()) {
      String s = (String)e.nextElement();
      if("method".equalsIgnoreCase(s)) {
         method = props.getProperty(s).toUpperCase();
         continue;
      }
      if(params.length() > 0) {
        params += "&";
      }
      params += s + "=" + props.getProperty(s).replace(" ", "%20").replace("&", "%26");
    }
    if(params.length() > 0) {
      path += "?" + params;
    }
    builder.node("Full rest url: " + path + "\nMethod: " + method);
    RestRead read = new RestRead(path, userInfo, method);
    builder.node("");
    builder.node("Result data: ");
    builder.node(read.getData());
    return builder;
  }

// http://localhost:8080/rest/private/bench/inject/userInject?users=demo&type=n&method=get


  @Usage("To get the data of rest url")
  @Man("""\
  GET DATA OF REST URL
  
  The first, must use command: rest use
  % rest use domain={domain name:http://localhost:8080};rest={rest name:rest} - to set information of server rest 

  The next, must login on the server rest
  % rest login -u {user's Id:root} -p {user's password:gtn}
    
  The default method is: rest get {path of rest} {some query params}
  % rest get /private/bench/inject/userInject users=demo,mary,john;type=n - inject user demo,mary,john with password is exo
  
  Command use for activity:
  % rest activity fetch {activityId} - get the activity
  % rest activity rm {activityId} - remove activity
  % rest activity like {activityId} like - like/unlike activity
  % rest activity comments {activityId} comments => return all 
  % rest activity comments {activityId}  limit=10;offset=0 => return list 
  % rest activity add {activityId}  text='the title' - post new a comment
  % rest activity update {activityId} {commentId} title='the new title' - update a comment
  % rest activity rm {activityId}  {commentId} - remove the comment

  Command use for Stream activities:
  % rest stream user {user's Id} - get the activities stream of user
  % rest stream user {user's Id} type=connections;after=xxx;before=yyy - get the activities stream of user with query params
  % rest stream space {space's Id} - get the activities stream of space

  Command use for User:
  % rest user {user's Id} - get the information of user
  % rest user {user's Id} c - get the connections of user
  % rest user q=xx* - find users by input query param

  """)
  @Command
  public Object main() {
    def builder = new UIBuilder();
    java.util.Properties props = System.getProperties();
    builder.node("Info: {domain : " + props.get("domain") + ", user: " + props.get("user") +"}");
    builder.node("Please, command: 'man rest'");
    return builder;
  }
  
   @Usage("To get the data of rest url for activities")
   @Man("""\
     action: The action to activie [fetch, like, rm, add, update, comments]
     activityId: The activity's Id
     commentId: The comment's Id, use for action on comments.
     parameters: The parameters
   
   """)
  @Command
  public Object activity (
    @Argument(unquote = false)
    @Usage("The action")
    @Man("The action [rm, like, comments, add, update]")
    String action,
    @Argument(unquote = false)
    @Usage("The activity's Id")
    @Man("Input activityId")
    String activityId,
    @Argument(unquote = true)
    @Usage("The comment's Id")
    @Man("The comment's Id")
    String commentId,
    @Argument(unquote = true)
    @Usage("Input the query parameters [ex: limit=10;offset=0;method=get]")
    @Man("The parameters of rest") Value.Properties parameters) {
    
    if(action == null || action.trim().length() == 0) {
      throw new ScriptException("The method input not found. Please, use method: fetch, like, rm, add, update, comments");
    }
    if(activityId == null || activityId.trim().length() == 0 || activityId.indexOf("=") > 0) {
       throw new ScriptException("Need input the activity's Id");
    }
    def path = "social/activities/" + activityId;
    if("fetch".equalsIgnoreCase(action) ) {
      return get(path, parameters);
    }
    //
    java.util.Properties sysProps = System.getProperties();
    def valueAction = sysProps.get(action);
    
    def params = (parameters != null && parameters.getProperties() != null) ? parameters.getString() : "";
    if(params == "" && commentId != null && commentId.indexOf("=") > 0) {
      params = commentId;
    }
    params += ";" + valueAction;
    
    if("like".equalsIgnoreCase(action) || "comments".equalsIgnoreCase(action)) {
      path += "/" + action;
    }
    if(("rm".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action)) && 
        commentId != null && commentId.trim().length() > 0 && commentId.indexOf("=") < 0) {
      path += "/comments/" + commentId;
    }
    //
    return get(path, new Value.Properties(params));
  }
  
  //Stream
   @Usage("To get the data of rest url for stream activities")
   @Man("""\
     type: The type of stream [users or spaces]
     id: The Id of space or user
     parameters: The parameters
   """)
  @Command
  public Object stream (
    @Argument(unquote = false)
    @Usage("The type of stream [user or space]")
    @Man("The type of stream [user or space]")
    String type,
    @Argument(unquote = true)
    @Usage("The Id of space or user")
    @Man("The Id of space or user")
    String id,
    @Argument(unquote = true)
    @Usage("Input the query parameters [ex: type=connections;after=xxx;before=yyy]")
    @Man("The parameters of rest") Value.Properties parameters) {
    //
    def path = "social/users/{param}/activities";
    def spacePath = "social/spaces/{param}/activities";
    if(type != null && ("user".equalsIgnoreCase(type) || "space".equalsIgnoreCase(type))) {
      if("space".equalsIgnoreCase(type)) {
         path = spacePath;
      }
    } else {
      throw new ScriptException("Need input type of activities stream: space or user");
    }
    if(id == null || id.trim().length() == 0) {
      throw new ScriptException("Need input the " + (("space".equalsIgnoreCase(type))  ? "space" : "user") + "'s Id.");
    }
    path = path.replace("{param}", id);
    //
    return get(path, parameters);
  }

  
  //Users
   @Usage("To get the data of rest url for users")
   @Man("""\
     userId: The user's Id
     connections: get connections of user, input is c
     parameters: The parameters
   """)
  @Command
  public Object user (
    @Argument(unquote = false)
    @Usage("The user's Id")
    @Man("The user's Id")
    String userId,
    @Argument(unquote = true)
    @Usage("get connections of user")
    @Man("Get connections of user, input char c")
    String connections,
    @Argument(unquote = true)
    @Usage("Input the query parameters [ex: q=test")
    @Man("The parameters of rest") Value.Properties parameters) {
    //
    def path = "social/users";
    def param = "";
    def isQuery = true;
    if(userId != null && userId.trim().length() > 0) {
      if(userId.indexOf("=") < 0) {
        path += "/" + userId;
        isQuery = false;
        if(connections != null && "c".equalsIgnoreCase(connections)) {
          path += "/connections";
        } else if(connections.indexOf("=") > 0) {
          param = connections;
        }
      } else {
        param = userId;
      }
    }
    if(parameters == null) {
      parameters = new Value.Properties(param);
    }
    System.out.println(String.valueOf(isQuery) + " " + parameters);
    if(parameters != null && isQuery && 
       (parameters.getProperties().get("q") == null || parameters.getProperties().get("q").length() == 0)){
      throw new ScriptException("Need input the user's Id or query param by cyntax: q=xxx");
    }
    //
    return get(path, parameters);
  }









}
