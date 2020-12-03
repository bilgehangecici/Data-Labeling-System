import java.util.ArrayList;
import java.util.Date;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DataLabelingSystem {

    private Dataset dataset = null;
    private ArrayList<User> userList = null;
    private Logger logger = Logger.getInstance();
    private String inputName,outputName;

    public void createDataset() {
        Dataset dataset = null;

        try {
            JSONParser parser = new JSONParser();

            Object obj = parser.parse(new FileReader(this.inputName));

            JSONObject jsonObject = (JSONObject) obj;
            
            int datasetId = ((Long) jsonObject.get("dataset id")).intValue();

            String datasetName = (String) jsonObject.get("dataset name");

            int maxNumberPerInstance = ((Long) jsonObject.get("maximum number of labels per instance")).intValue();
            if(maxNumberPerInstance < 1) {
            	throw new Exception ("Maximum number of labels per instance is not valid.");
            }
            dataset = new Dataset(datasetId, datasetName, maxNumberPerInstance);

            JSONArray classLabelList = (JSONArray) jsonObject.get("class labels");
            JSONArray instanceList = (JSONArray) jsonObject.get("instances");

            @SuppressWarnings("unchecked")
            Iterator<JSONObject> instanceIterator = instanceList.iterator();
            @SuppressWarnings("unchecked")
            Iterator<JSONObject> labelIterator = classLabelList.iterator();

            while (instanceIterator.hasNext()) {
                JSONObject instanceObj = (instanceIterator.next());
                String instanceText = (String) instanceObj.get("instance");
                int instanceID = ((Long) instanceObj.get("id")).intValue();
               
                dataset.addInstance(new Instance(instanceText, instanceID));
            }

            while (labelIterator.hasNext()) {
                JSONObject labelObj = (labelIterator.next());
                String labelText = (String) labelObj.get("label text");
                int labelID = ((Long) labelObj.get("label id")).intValue();
                
                dataset.addLabel(new Label(labelID, labelText));
            }

        } catch (Exception e) {
        	logger.error(new Date(),e.toString());
        	System.exit(0);
        }

        this.dataset = dataset;
    }

    public void loadConfig(String fileName) {
        ArrayList<User> userList = new ArrayList<User>();
        try {
            JSONParser parser = new JSONParser();

            Object obj = parser.parse(new FileReader(fileName));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray userObjects = (JSONArray) jsonObject.get("users");
            this.inputName =(String)jsonObject.get("input_name");
            this.outputName = (String) jsonObject.get("output_name");

            @SuppressWarnings("unchecked")
            Iterator<JSONObject> userListIterator = userObjects.iterator();
            while (userListIterator.hasNext()) {

                JSONObject userObj = (userListIterator.next());
                int userID = ((Long) userObj.get("user id")).intValue();
                String userName = (String) userObj.get("user name");
                String userType = (String) userObj.get("user type");

                userList.add(new RandomBot(userName, userID, userType));
                
            }

        } catch (Exception e) {
        	logger.error(new Date(),e.toString());
        	System.exit(1);
        }
        
        this.userList = userList;
    }

    public void writeOutputFile() {
        ArrayList<Assignment> assignmentList = dataset.getAssignmentList();
        ArrayList<Instance> instanceList = dataset.getInstances();
        ArrayList<Label> labelList = dataset.getClassLabels();
        
        
        
        
        
        JSONObject datasetObject = new JSONObject();
        datasetObject.put("dataset id", dataset.getDatasetID());
        datasetObject.put("dataset name", dataset.getDatasetName());
        datasetObject.put("maximum number of labels per instance", dataset.getMaxLabelPerInstance());

        JSONArray classLabelArray = new JSONArray();

        for (int j = 0; j < labelList.size(); j++) {
            JSONObject classLabelObject = new JSONObject();
            classLabelObject.put("label id: ", labelList.get(j).getLabelID());
            classLabelObject.put("label text: ", labelList.get(j).getLabelName());
            classLabelArray.add(classLabelObject);
        }
        datasetObject.put("class labels", classLabelArray);

        JSONArray instanceArray = new JSONArray();

        for (int j = 0; j < instanceList.size(); j++) {
            JSONObject instanceObject = new JSONObject();
            instanceObject.put("id: ", instanceList.get(j).getInstanceID());
            instanceObject.put("instance : ", instanceList.get(j).getContent());
            instanceArray.add(instanceObject);
        }
        datasetObject.put("instances", instanceArray);

        JSONArray assignmentJSONList = new JSONArray();

        for (int j = 0; j < assignmentList.size(); j++) {
            JSONObject assignmentObject = new JSONObject();
            assignmentObject.put("instance id:", assignmentList.get(j).getInstance().getInstanceID());
            JSONArray classLabelIds = new JSONArray();

            for (int i = 0; i < assignmentList.get(j).getAssignedLabels().size(); i++)                
            	classLabelIds.add(assignmentList.get(j).getAssignedLabels().get(i).getLabelID());
            
            String date = assignmentList.get(j).getFormattedTime();
            String formattedDate = date.replace('/','.');
   
            assignmentObject.put("class label ids:", classLabelIds);
            assignmentObject.put("user id:", assignmentList.get(j).getUser().getUserID());
            assignmentObject.put("datetime:", formattedDate);
            

            assignmentJSONList.add(assignmentObject);
        }
        datasetObject.put("class label assignments", assignmentJSONList);

        JSONArray userArray = new JSONArray();

        for (int j = 0; j < userList.size(); j++) {
            JSONObject userObject = new JSONObject();
            userObject.put("user id: ", userList.get(j).getUserID());
            userObject.put("user name: ", userList.get(j).getUserName());
            userObject.put("user type: ", userList.get(j).getUserType());
            userArray.add(userObject);
        }
        datasetObject.put("users", userArray);
        
        try (FileWriter file = new FileWriter(this.outputName)) {

            file.write(datasetObject.toJSONString());
            file.flush();

        } catch (IOException e) {
        	logger.error(new Date(),e.toString());
        	System.exit(2);
        }

    }

    public Dataset getDataset() {
        return this.dataset;
    }

    public ArrayList<User> getUserList() {
        return this.userList;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

}