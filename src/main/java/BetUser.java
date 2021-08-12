import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

@Getter @Setter
@AllArgsConstructor
@EqualsAndHashCode
public class BetUser {
    private User user;
    private Integer point;
}
