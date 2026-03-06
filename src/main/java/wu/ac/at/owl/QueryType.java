package wu.ac.at.owl;

public enum  QueryType {
	    OBLIGATION_STATE,
	    REGULATED_ACTION_STATE,
	    TEMPORAL_ACTION_STATE,
	    EVENT_STATE,
		ENTITY,
		ACTION,
		RESOURCE;

	    public static QueryType fromKeyword(String keyword) {
	        switch (keyword.toLowerCase()) {
	            case "obligation_state":
	                return OBLIGATION_STATE;

	            case "regulated_action_state":
	                return REGULATED_ACTION_STATE;

	            case "temporal_action_state":
	                return TEMPORAL_ACTION_STATE;

	            case "event_state":
	                return EVENT_STATE;
	                
	            case "entity":
	            	return ENTITY;
	            case "action":
	            	return ACTION;
	            case "resource":
	            	return RESOURCE;

	            default:
	                throw new IllegalArgumentException(
	                        "Unknown query keyword: " + keyword);
	        }
	    }
}
