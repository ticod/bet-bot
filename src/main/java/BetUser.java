import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Getter @Setter
@AllArgsConstructor
public class BetUser {
    private User user;
    private Integer point;
}
