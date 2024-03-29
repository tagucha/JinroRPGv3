package net.tagucha.jrpg.item;

import org.bukkit.ChatColor;
import net.tagucha.jrpg.core.GameTimer;
import net.tagucha.jrpg.JinroRPG;

public enum TimePermission {
    ANYTIME(){
        @Override
        public boolean canUse(GameTimer.Clock clock) {
            return true;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sはいつでも使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s"
            );
        }
    },
    DAYTIME() {
        @Override
        public boolean canUse(GameTimer.Clock clock) {
            return clock == GameTimer.Clock.DAY;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sは昼間のみ使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s"
            );
        }
    },
    NIGHT() {
        @Override
        public boolean canUse(GameTimer.Clock clock) {
            return clock == GameTimer.Clock.NIGHT;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sは夜にのみ使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s"
            );
        }
    };

    public abstract boolean canUse(GameTimer.Clock clock);

    public abstract String getErrorMessage();
}
