package scholarship.model;

public class CommitteeMember extends User {
    private String committeeID;
    private String position;

    public CommitteeMember(int id, String fullName, String email, boolean isActive, String committeeID,
            String position) {
        super(id, fullName, email, "Committee", isActive);
        this.committeeID = committeeID;
        this.position = position;
    }

    public CommitteeMember() {
        super();
        this.role = "Committee";
    }

    public String getCommitteeID() {
        return committeeID;
    }

    public void setCommitteeID(String committeeID) {
        this.committeeID = committeeID;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean login() {
        return true;
    }

    @Override
    public void logout() {
    }
}
