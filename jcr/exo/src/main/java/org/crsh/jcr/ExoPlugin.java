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
package org.crsh.jcr;

import java.lang.reflect.Method;
import java.util.Map;
import javax.jcr.Repository;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoPlugin extends JCRPlugin<ExoPlugin> {

  @Override
  public String getName() {
    return "exo";
  }

  @Override
  public String getDisplayName() {
    return "Exo JCR plugin";
  }

  @Override
  public String getUsage() {
    return "You can use a container bound repository: 'repo use container=portal'";
  }

  @Override
  public ExoPlugin getImplementation() {
    return this;
  }

  @Override
  public Repository getRepository(Map<String, String> properties) throws Exception {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    // Get top container
    Class<?> eXoContainerContextClass = cl.loadClass("org.exoplatform.container.ExoContainerContext");
    Method getTopContainerMethod = eXoContainerContextClass.getMethod("getTopContainer");
    Object topContainer = getTopContainerMethod.invoke(null);

    //
    if (topContainer != null) {
			String containerName = null;
			String repositoryName = null;
			if(properties == null) {
				java.util.Properties props = System.getProperties()
				containerName = props.get("container");
				repositoryName = props.get("repository");
			} else {
				containerName = properties.get("container");
				repositoryName = properties.get("repository");
			}
       
      Object container;
      if (containerName != null) {
        Method getPortalContainerMethod = topContainer.getClass().getMethod("getPortalContainer", String.class);
        container = getPortalContainerMethod.invoke(topContainer, containerName);
      } else {
        container = topContainer;
      }

      //
      if (container != null) {
        Method getComponentInstanceOfTypeMethod = container.getClass().getMethod(
            "getComponentInstanceOfType", Class.class);
        Class<?> repositoryServiceClass = Thread.currentThread().getContextClassLoader().loadClass(
            "org.exoplatform.services.jcr.RepositoryService");
        Object repositoryService = getComponentInstanceOfTypeMethod.invoke(container,
            repositoryServiceClass);

        //
        if (repositoryService != null) {
          Repository repository = null;
          if (repositoryName != null) {
            try {
              Method getRepositoryMethod = repositoryService.getClass().getMethod("getRepository");
              repository = (Repository) getRepositoryMethod.invoke(repositoryService, repositoryName);
            } catch (Exception e) {
              System.out.println("\n Can not get repository by name: " + repositoryName + "\n" + e.getMessage());
            }
          }
          if (repository == null) {
            Method getCurrentRepositoryMethod = repositoryService.getClass().getMethod("getCurrentRepository");
            repository = (Repository) getCurrentRepositoryMethod.invoke(repositoryService);
          }
          return repository;
        }
      }
    }

    //
    return null;
  }
}
