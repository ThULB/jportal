package fsu.jportal.constants;

public enum ParticipantRoleTypes{
    operator,
    ownerEntity,
    sponsorEntity,
    partnerEntity;
    
    public static boolean equals(String type){
        for (ParticipantRoleTypes participantType : ParticipantRoleTypes.values()) {
            if(type.equals(participantType.toString())){
                return true;
            }
        }
        return false;
    }
}