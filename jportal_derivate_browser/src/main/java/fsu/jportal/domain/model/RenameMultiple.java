package fsu.jportal.domain.model;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by chi on 18.03.20
 *
 * @author Huu Chi Vu
 */
public class RenameMultiple {
    private String derivId;
    private String newName;
    private String fileName;
    private String pattern;
    private ArrayList<Response> responses;

    public String getDerivId() {
        return derivId;
    }

    public void setDerivId(String derivId) {
        this.derivId = derivId;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void add(Response resp){
        this.responses.add(resp);
    }

    public Response[] getResponseList(){
        if(responses == null){
            responses = new ArrayList<>();
        }
        return responses.toArray(new Response[responses.size()]);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static class Response {
        private String oldName;
        private String newName;

        private Response() {
        }

        public Response(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }

        public String getOldName() {
            return oldName;
        }

        public void setOldName(String oldName) {
            this.oldName = oldName;
        }

        public String getNewName() {
            return newName;
        }

        public void setNewName(String newName) {
            this.newName = newName;
        }
    }
}
