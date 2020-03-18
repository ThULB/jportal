package fsu.jportal.domain.model;

/**
 * Created by chi on 17.03.20
 *
 * @author Huu Chi Vu
 */
public class MoveDocs {
    private MoveDoc docs[];

    public MoveDoc[] getDocs() {
        return docs;
    }

    public void setDocs(MoveDoc[] docs) {
        this.docs = docs;
    }


    public static class MoveDoc {
        private String objId;
        private String newParentId;
        private boolean success;

        private MoveDoc() {
        }

        public MoveDoc(String objId, String newParentId) {
            this.objId = objId;
            this.newParentId = newParentId;
        }

        public String getObjId() {
            return objId;
        }

        public void setObjId(String objId) {
            this.objId = objId;
        }

        public String getNewParentId() {
            return newParentId;
        }

        public void setNewParentId(String newParentId) {
            this.newParentId = newParentId;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}
