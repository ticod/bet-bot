import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class BetInfo {
    private String title;
    private User user;
    private Integer totalPoint;
    private Map<User, Integer> agree;
    private Map<User, Integer> disagree;
    private BetStatus betStatus;

    public BetInfo() {
        this.title = "";
        this.user = null;
        this.totalPoint = 0;
        this.agree = new HashMap<>();
        this.disagree = new HashMap<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTotalPoint() {
        return totalPoint;
    }

    public void setTotalPoint(Integer totalPoint) {
        this.totalPoint = totalPoint;
    }

    public Map<User, Integer> getAgree() {
        return agree;
    }

    public void setAgree(Map<User, Integer> agree) {
        this.agree = agree;
    }

    public Map<User, Integer> getDisagree() {
        return disagree;
    }

    public void setDisagree(Map<User, Integer> disagree) {
        this.disagree = disagree;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BetStatus getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(BetStatus betStatus) {
        this.betStatus = betStatus;
    }

    public void clear() {
        this.title = "";
        this.user = null;
        this.totalPoint = 0;
        this.agree.clear();
        this.disagree.clear();
    }
}
