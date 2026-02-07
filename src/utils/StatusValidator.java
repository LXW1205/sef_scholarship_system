package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusValidator {

    // Application Statuses
    public static final String APP_PENDING = "Pending";
    public static final String APP_REVIEWING = "Reviewing";
    public static final String APP_REVIEWED = "Reviewed";
    public static final String APP_PENDING_INTERVIEW = "Pending Interview";
    public static final String APP_SHORTLISTED = "Shortlisted"; // Deprecated/Legacy
    public static final String APP_INTERVIEWED = "Interviewed";
    public static final String APP_AWARDED = "Awarded";
    public static final String APP_REJECTED = "Rejected";
    public static final String APP_WITHDRAWN = "Withdrawn";

    // Evaluation Statuses
    public static final String EVAL_PENDING = "Pending";
    public static final String EVAL_COMPLETED = "Completed";

    // Interview Statuses
    public static final String INT_SCHEDULED = "Scheduled";
    public static final String INT_COMPLETED = "Completed";
    public static final String INT_CANCELLED = "Cancelled";
    public static final String INT_RESCHEDULED = "Rescheduled";

    // Clarification Statuses
    public static final String CLAR_PENDING = "Pending";
    public static final String CLAR_ANSWERED = "Answered";

    private static final Map<String, List<String>> APP_TRANSITIONS = new HashMap<>();
    private static final Map<String, List<String>> EVAL_TRANSITIONS = new HashMap<>();
    private static final Map<String, List<String>> INT_TRANSITIONS = new HashMap<>();
    private static final Map<String, List<String>> CLAR_TRANSITIONS = new HashMap<>();

    static {
        // Application Transitions
        APP_TRANSITIONS.put(APP_PENDING, Arrays.asList(APP_REVIEWING, APP_WITHDRAWN));
        APP_TRANSITIONS.put(APP_REVIEWING, Arrays.asList(APP_REVIEWED, APP_WITHDRAWN));
        // Reviewed -> Pending Interview (Logic: "Schedule Interview") or Rejected
        // Reviewed -> Pending Interview OR Shortlisted (Legacy) OR Rejected
        APP_TRANSITIONS.put(APP_REVIEWED,
                Arrays.asList(APP_PENDING_INTERVIEW, APP_SHORTLISTED, APP_REJECTED, APP_WITHDRAWN));

        // Pending Interview -> Interviewed (Auto-update) or Rejected/Withdrawn
        APP_TRANSITIONS.put(APP_PENDING_INTERVIEW, Arrays.asList(APP_INTERVIEWED, APP_REJECTED, APP_WITHDRAWN));

        // Legacy compatibility
        APP_TRANSITIONS.put(APP_SHORTLISTED,
                Arrays.asList(APP_PENDING_INTERVIEW, APP_INTERVIEWED, APP_AWARDED, APP_REJECTED, APP_WITHDRAWN));

        APP_TRANSITIONS.put(APP_INTERVIEWED, Arrays.asList(APP_AWARDED, APP_REJECTED, APP_WITHDRAWN));
        // States like Awarded, Rejected, Withdrawn are terminal or have no outgoing
        // transitions in this scope

        // Evaluation Transitions
        EVAL_TRANSITIONS.put(EVAL_PENDING, Arrays.asList(EVAL_COMPLETED, "Approved", "Rejected", "Under Review"));
        // Note: ApplicationHandler uses recommendation as Evaluation status

        // Interview Transitions
        INT_TRANSITIONS.put(INT_SCHEDULED, Arrays.asList(INT_COMPLETED, INT_CANCELLED, INT_RESCHEDULED));
        INT_TRANSITIONS.put(INT_RESCHEDULED, Arrays.asList(INT_COMPLETED, INT_CANCELLED, INT_RESCHEDULED));

        // Clarification Transitions
        CLAR_TRANSITIONS.put(CLAR_PENDING, Arrays.asList(CLAR_ANSWERED));
    }

    public static boolean isValidTransition(String currentStatus, String nextStatus, String entityType) {
        if (currentStatus == null || nextStatus == null)
            return false;
        if (currentStatus.equals(nextStatus))
            return true; // No change is always valid

        Map<String, List<String>> transitions;
        switch (entityType.toLowerCase()) {
            case "application":
                transitions = APP_TRANSITIONS;
                break;
            case "evaluation":
                transitions = EVAL_TRANSITIONS;
                break;
            case "interview":
                transitions = INT_TRANSITIONS;
                break;
            case "clarification":
                transitions = CLAR_TRANSITIONS;
                break;
            default:
                return false;
        }

        List<String> allowed = transitions.get(currentStatus);
        return allowed != null && allowed.contains(nextStatus);
    }
}
