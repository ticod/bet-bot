import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class BetInfo {
    private String title;
    private User user;
    private Integer totalPoint;
    private Map<User, Integer> agree;
    private Map<User, Integer> disagree;
    private BetStatus betStatus = BetStatus.INITIALIZE;

    public BetInfo() {
        this.title = "";
        this.user = null;
        this.totalPoint = 0;
        this.agree = new HashMap<>();
        this.disagree = new HashMap<>();
    }

    public void clear() {
        this.title = "";
        this.user = null;
        this.totalPoint = 0;
        this.agree.clear();
        this.disagree.clear();
    }

    public boolean checkBetting(User user) {
        return agree.get(user) != null || disagree.get(user) != null;
    }
}
