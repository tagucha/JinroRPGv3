package net.tagucha.jrpg.item;

import org.bukkit.ChatColor;
import net.tagucha.jrpg.job.GameJob;
import net.tagucha.jrpg.JinroRPG;

public enum ItemPermission {
    ANYONE(){
        @Override
        public boolean canUse(GameJob job) {
            return true;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sは誰でも使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s");
        }
    },
    WEREWOLF() {
        @Override
        public boolean canUse(GameJob job) {
            return job.side == 1;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sは人狼のみが使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s");
        }
    },
    NOT_WEREWOLF() {
        @Override
        public boolean canUse(GameJob job) {
            return job.side != 1;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sは人狼以外が使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s");
        }
    },
    MAD() {
        @Override
        public boolean canUse(GameJob job) {
            return job == GameJob.MAD;
        }

        @Override
        public String getErrorMessage() {
            return String.format("%s%s%sは共犯者のみが使えます",
                    JinroRPG.getChatLogo(ChatColor.RED),
                    ChatColor.DARK_RED,
                    "%s");
        }
    };

    public abstract boolean canUse(GameJob job);

    public abstract String getErrorMessage();
}
