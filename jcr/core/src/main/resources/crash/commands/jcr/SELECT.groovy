import javax.jcr.query.Query;
import org.crsh.shell.ui.UIBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Option;

public class select extends org.crsh.command.CRaSHCommand {

  @Usage("execute a JCR sql query")
  @Command
  @Man("""Queries in SQL format are possible via the ##select## command. You can write a query with the same syntax defined
by the specification and add options to control the number of results returned. By default the number of nodes is limited
to 5 results:

[/]% select * from nt:base
The query matched 1114 nodes
+-/
| +-properties
| | +-jcr:primaryType: nt:unstructured
| | +-jcr:mixinTypes: [exo:owneable,exo:privilegeable]
| | +-exo:owner: '__system'
| | +-exo:permissions: [any read,*:/platform/administrators read,*:/platform/administrators add_node,*:/platform/administratorsset_property,*:/platform/administrators remove]
+-/workspace
| +-properties
| | +-jcr:primaryType: mop:workspace
| | +-jcr:uuid: 'a69f226ec0a80002007ca83e5845cdac'
...

Display 20 nodes from the offset 10:

[/]% select * from nt:base -o 10 -l 20
The query matched 1114 nodes
...

It is possible also to remove the limit of displayed nodes with the -a option (you should use this option with care) :

[/]% select * from nt:base -a
The query matched 1114 nodes
...

select is a <Void,Node> command producing all the matched nodes.""")
  public void main(
    InvocationContext<Void, Node> context,
//        @Option(names=["o","offset"])
//        @Usage("the result offset")
//        @Man("The offset of the first node to display")
//        Integer offset,
//        @Option(names=["l","limit"])
//        @Usage("the result limit")
//        @Man("The number of nodes displayed, by default this value is equals to 5")
//        Integer limit,
//        @Option(names=["a","all"])
//        @Usage("ignore the limit argument")
//        @Man("Display all the results by ignoring the limit argument, this should be used with care for large result set")
//        Boolean all,
    @Argument(unquote = false)
    @Usage("the query")
    @Man("The query, as is")
    List<String> query) {
    //
    String statement = "SELECT", ex = "", qr="";
    int offset = 0, limit = 0, i = 0;
    def all = false, add = true, dName = false, h = false;
    //
    try {
      query.each {
        qr += " " + it ;
      }
      //
      query.each {
        if(it.equals('-a')) {
          all = true;
          add = false;
        } else if(it.equals('-o')) {
          offset = java.lang.Integer.valueOf(query.get(i + 1));
          add = false;
        } else if(it.equals('-l')) {
          limit = java.lang.Integer.valueOf(query.get(i + 1));
          add = false;
        } else if(it.equals('-n')) {
          dName = true;
          add = false;
        } else if(it.equals('-h') || it.equals('--help')) {
          h = true;
        }
        if(add) {
          statement += " " + it ;
        }
        ++i;
      };
    } catch (Exception e) {
      h = true;
      ex = e.toString();
    }
    
    //
    def builder = new UIBuilder();
    //
    if(h || statement.toUpperCase().indexOf("FROM") < 0) {
      if(ex.length() > 0) {
        builder.node("Failed to build query: select" + qr + "\nProblem: " + ex+ "\n");
      }
      builder.node("Building query by SQL to get nodes.(Ex: select * from exo:forum -a -o 10 -l 20 -n)");
      builder.node("   -a: Display all nodes of result, ignore param -o and -l");
      builder.node("   -o: The offset value to display result (from = offset; to = offset + limit)");
      builder.node("   -l: The limit value to limit max display items on result");
      builder.node("   -n: Display list node name of result");
      //
      context.writer.print(builder);
      return;  
    }

    assertConnected();
    //
    offset = offset ?: 0;
    limit = limit ?: 0;
    all = all ?: false;

    //
    if (offset < 0) {
      throw new ScriptException("No negative offset accepted; $offset");
    }

    //
    if (limit < 0) {
      throw new ScriptException("No negative limit accepted: -limit");
    }

    //
    if (all) {
      limit = 0;
    }

    //
    def queryMgr = session.workspace.queryManager;

    statement = statement.replace(" and ", " AND ").replace(" or ", " OR ").replace(" and(", " AND (").replace(" or(", " OR (");
    statement = statement.replace(" where ", " WHERE ").replace(" order by ", " ORDER BY ").replace(" from ", " FROM ");
    
    System.out.println("\nExecute sql query: " + statement + "\nLimit: " + limit + "  Offset: " + offset);
    
    
    
    //
    def queryImpl = queryMgr.createQuery(statement, Query.SQL);
    if (limit > 0) {
        queryImpl.setOffset(offset);
        queryImpl.setLimit(limit);
    }

    //
    def result = queryImpl.execute();

    //
    def nodes = result.nodes;
    def total = nodes.size;

    if (all) {
      limit = total;
    } else if (limit == 0) {
      limit = 5;
    }
    //
    List<String> nodeNames = new ArrayList<String>();

    builder.node("The query matched " + total + " nodes, max print: " + limit + " nodes.") {
      def index = 0;
      while (nodes.hasNext() && index < limit) {
        def n = nodes.next();
        nodeNames.add(n.getName());
        //
        formatNode(builder, n, 0, 1);

        //
        context.produce(n);

        //
        index++;
      }
    }

    if(dName == true) {
      builder.node( "\n  +-nodes-name" );
      for(String str : nodeNames) {
        builder.node("    +-" + str );
      }
    }
    
    //
    context.writer.print(builder);
  }
}
