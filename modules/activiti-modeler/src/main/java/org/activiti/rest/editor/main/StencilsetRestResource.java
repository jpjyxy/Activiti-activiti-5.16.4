/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.rest.editor.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author Tijs Rademakers
 */
@RestController
public class StencilsetRestResource
{
    @Autowired
    public ObjectMapper objectMapper;
    
    @RequestMapping(value = "/editor/stencilset", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    String getStencilset()
    {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
        try
        {
            JsonNode stencilsetContent = objectMapper.readTree(stencilsetStream);
            String stencilsetCon = getResources(stencilsetContent);
            return stencilsetCon;
        }
        catch (Exception e)
        {
            throw new ActivitiException("Error while loading stencil set", e);
        }
    }
    
    public String getResources(JsonNode stencilsetContent)
        throws IOException
    {
        String stencilPath = this.getClass().getClassLoader().getResource("generate").getPath();
        File[] fileUtils = new File(stencilPath).listFiles();
        for (File file : fileUtils)
        {
            File[] listFile = new File(file.getAbsolutePath()).listFiles();
            for (File jsonFile : listFile)
            {
                JsonNode fileNode = objectMapper.readTree(FileUtils.readFileToString(jsonFile));
                String title = fileNode.get("node").get("title").asText();
                String fileName = jsonFile.getName().substring(0, jsonFile.getName().indexOf("."));
                // 1. classInfo
                String className = "servicetaskbase_" + fileName;
                String classValue = fileNode.get("node").get("class").asText();
                String view = fileNode.get("node").get("view").asText();
                String icon = fileNode.get("node").get("icon").asText();
                String extensionId = fileNode.get("node").get("extensionId").asText();
                String groupName = extensionId.substring(0, extensionId.indexOf("_"));
                String classCon =
                    "{" + "\"name\": \"" + className + "\", " + "\"properties\": [" + "{"
                        + "\"id\": \"servicetaskclass\", " + "\"type\": \"String\", " + "\"title\": \"Class\", "
                        + "\"value\": \"" + classValue + "\", "
                        + "\"description\": \"Class that implements the service task logic.\", " + "\"popular\": true"
                        + "}" + "]" + "}";
                // 2. nodeInfo
                String nodeInfo =
                    "{" + "\"type\": \"node\"," + "\"id\": \"ServiceTask\"," + "\"title\": \"" + title + "\","
                        + "\"description\": \"An automatic task with service logic\"," + "\"view\": \"" + view + "\","
                        + "\"icon\": \"" + icon + "\"," + "\"groups\": [" + "\"" + groupName + "\"" + "],"
                        + "\"propertyPackages\": [" + "\"elementbase\"," + "\"baseattributes\"," + "\"" + className
                        + "\"," + "\"servicetaskpluginbase\"," + "\"asynchronousbase\"," + "\"executionlistenersbase\","
                        + "\"loopcharacteristics\"," + "\"activity\"" + "]," + "\"roles\": [" + "\"sequence_start\","
                        + "\"Activity\"," + "\"sequence_end\"," + "\"ActivitiesMorph\"," + "\"all\""
                        + "],\"extensionId\":\"" + extensionId + "\"" + "}";
                ArrayNode packageNode = (ArrayNode)stencilsetContent.get("propertyPackages");
                packageNode.add(objectMapper.readTree(classCon));
                ArrayNode arrayNode = (ArrayNode)stencilsetContent.get("stencils");
                arrayNode.add(objectMapper.readTree(nodeInfo));
            }
        }
        return stencilsetContent.toString();
    }
}
