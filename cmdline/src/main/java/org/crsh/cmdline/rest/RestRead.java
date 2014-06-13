/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.crsh.cmdline.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

public class RestRead {
  String url, userInfo, method;
  public RestRead(String url, String userInfo, String method) {
    this.url = url;
    this.userInfo = userInfo;
    this.method = method;
  }
  
  public String getData() {
    InputStream input = null;
    try {
      URL url = new URL(this.url);
      //
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod(method);
      connection.setDoOutput(true);
      if (userInfo != null && userInfo.length() > 0) {
        String authEncoded = new String(Base64.encodeBase64(userInfo.getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
      }
      //
      input = (InputStream) connection.getInputStream();
      return readTextContent(input);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (Exception e2) {
        }
      }
    }
    return "";
  }

  static public String readTextContent(InputStream input) {
    InputStreamReader reader = new InputStreamReader(input);
    return readTextContent(new BufferedReader(reader));
  }

  static public String readTextContent(BufferedReader input) {
    StringBuilder contents = new StringBuilder();
    try {
      String line = null;
      while ((line = input.readLine()) != null) {
        contents.append(line).append("\n");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return contents.toString();
  }
}
