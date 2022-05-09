package patchfilter.model.experiment;

import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import patchfilter.configuration.Constant;
import patchfilter.model.entity.CorrectPatch;
import patchfilter.model.entity.LineInfo;
import patchfilter.model.entity.Patch;
import patchfilter.model.entity.Project;
import patchfilter.model.service.QueryService;
import patchfilter.model.util.FileIO;

import java.util.List;

@Slf4j
@Builder
@Data
public class TraceQuery {


    private Project subject;


    public TraceQuery(Project subject) {
        this.subject = subject;
    }


    private boolean containsCorretPatch() {
        String correctPatchString = FileIO.readFileToString(Constant.CorrectPatchInfo + "patchinfo.json");
        List<CorrectPatch> correctPatchList = JSONObject.parseArray(correctPatchString, CorrectPatch.class);
        boolean contains = false;
        List<Patch> patchFileList = subject.getPatchList();
        for (Patch patchFile : patchFileList) {
            CorrectPatch correctPatch = new CorrectPatch(patchFile.getPatchName(), subject.getName(), String.valueOf(subject.getId()));
            if (correctPatchList.contains(correctPatch)) {

                patchFile.setCorrectness(true);
                contains = true;
            }
        }
        return contains;
    }


    public void queryProcess() {
    	StringBuilder resultBuilder = new StringBuilder();
    	String resultPath = "./test.csv";
    	
    	resultBuilder.append(subject.getName()+subject.getId()).append(",").append(subject.getPatchList().size()).append(",");

        QueryService queryService = new QueryService(subject);
        queryService.initSet();
        int queryNumber = 0;
        
        
        while (!queryService.isTerminate()) {
        	
            if (!containsCorretPatch()) {
                log.error("need manually check");
                break;
            }
            
            queryNumber++;
            LineInfo currentLine = queryService.pickOne();

            log.info("----------- QueryNumber: " + queryNumber);
            log.info("Pick line info {}", currentLine.getLineName(), currentLine.getPatchList().toString());

            boolean isTrue = false;
            for (Patch patchFile : currentLine.getPatchList()) {
                if (patchFile.isCorrectness()) {
                    isTrue = true;
                    break;
                }
            }
            
            log.info("Answer: " + isTrue);
            if (currentLine != null && !isTrue) {
                queryService.processAfterWrongTrace(currentLine);
            } else if (currentLine != null && isTrue) {
                queryService.processAfterRightTrace(currentLine);
            }
            //processMap = queryService.getProcessMap();
            //patchList = queryService.getPatchList();

            log.info("ProcessList Size: " + queryService.getProcessList().size());
            log.info("PatchList Size: " + queryService.getCandidatePatchList().size());
            log.info("QueryNumber: " + queryNumber);
            resultBuilder.append(queryService.getCandidatePatchList().size()).append(",");
            //log.info("PatchList: " + patchList.toString());
            //queryResult = queryResult + patchList.size() + ",";
            //restore(subject, currentLine);
            
        }
        resultBuilder.append("\n");
        FileIO.writeStringToFile(resultPath, resultBuilder.toString(), true);
    }


    public static void main(String[] args) {
        String project = "Chart";
        int Start = 1;
        int end = 26;
        for (int i = Start; i <= end; i++) {
            Project subject = new Project(project, i);
            if (subject.initPatchListByPath(Constant.AllPatchPath)) {
                log.info("Process " + subject.toString());
                TraceQuery traceQuery = new TraceQuery(subject);
                traceQuery.queryProcess();

            }
        }
    }

}
