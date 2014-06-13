/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.cmdline;

import junit.framework.TestCase;

import java.io.IOException;

import org.crsh.cmdline.rest.RestRead;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UtilTestCase extends TestCase {

  public void testIndent() throws Exception {
    assertIndent("", "");
    assertIndent("_a", "a");
    assertIndent("_a", " a");
    assertIndent("\n_a", "\na");
    assertIndent("\n_a", "\n a");
    assertIndent("\n\n_a", "\n\na");
    assertIndent("\n_a\n_b", "\n a\n  b");
  }

  private void assertIndent(String expected, String s) throws IOException {
    StringBuilder sb = Util.indent("_", s, new StringBuilder());
    assertEquals(expected, sb.toString());
  }
  
  public void testRest() {
    String url = "http://localhost:8080/rest/private/bench/inject/forumProfile?number=1&prefix=user";
    RestRead read = new RestRead(url, "root:gatein");
    System.out.println("the data: " + read.getData());
    
  }
}
