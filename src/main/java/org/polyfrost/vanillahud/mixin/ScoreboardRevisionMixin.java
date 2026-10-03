package org.polyfrost.vanillahud.mixin;

//? if > 1.8.9 {
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
//?} else {
/*import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.scoreboard.ServerScoreboard;
*///?}
import org.polyfrost.vanillahud.hud.HudInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardRevisionMixin {
    //? if > 1.8.9 {
    @Inject(
            method = {
                    "onObjectiveAdded",
                    "onObjectiveChanged",
                    "onObjectiveRemoved",
                    "onScoreChanged",
                    "onScoreLockChanged",
                    "onPlayerRemoved",
                    "onPlayerScoreRemoved",
                    "onTeamAdded",
                    "onTeamChanged",
                    "onTeamRemoved",
                    "setDisplayObjective",
                    "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V"
            },
            at = @At("HEAD"),
            require = 12
    )
    private void vanillahud$bump(CallbackInfo ci) {
        bump();
    }

    @Inject(method = "addPlayerToTeam", at = @At("HEAD"))
    private void vanillahud$bumpMembership(String player, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
        bump();
    }

    private void bump() {
        if (!((Object) this instanceof ServerScoreboard)) HudInternals.bumpScoreboardRevision();
    }
    //?} else {
    /*// 1.8.9 names these differently and has no score lock or per player score removal.
    // removeMemberFromTeam has a boolean returning overload, so it needs its own callback.
    @Inject(
            method = {
                    "onObjectiveCreated",
                    "onObjectiveUpdated",
                    "onObjectiveRemoved",
                    "onScoreUpdated",
                    "onScoresRemoved",
                    "onScoreRemoved",
                    "onTeamAdded",
                    "onTeamUpdated",
                    "onTeamRemoved",
                    "setDisplayObjective",
                    "removeMemberFromTeam(Ljava/lang/String;Lnet/minecraft/scoreboard/team/Team;)V"
            },
            at = @At("HEAD"),
            require = 11
    )
    private void vanillahud$bump(CallbackInfo ci) {
        bump();
    }

    @Inject(method = "removeMemberFromTeam(Ljava/lang/String;)Z", at = @At("HEAD"), require = 1)
    private void vanillahud$bumpTeamLeave(String player, CallbackInfoReturnable<Boolean> cir) {
        bump();
    }

    @Inject(method = "addMemberToTeam", at = @At("HEAD"), require = 1)
    private void vanillahud$bumpMembership(String player, String team, CallbackInfoReturnable<Boolean> cir) {
        bump();
    }

    private void bump() {
        if (!((Object) this instanceof ServerScoreboard)) HudInternals.bumpScoreboardRevision();
    }
    *///?}
}
