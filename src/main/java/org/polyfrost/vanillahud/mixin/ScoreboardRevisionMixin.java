package org.polyfrost.vanillahud.mixin;

import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.polyfrost.vanillahud.hud.HudInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardRevisionMixin {
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
        HudInternals.bumpScoreboardRevision();
    }

    @Inject(method = "addPlayerToTeam", at = @At("HEAD"))
    private void vanillahud$bumpMembership(String player, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
        HudInternals.bumpScoreboardRevision();
    }
}
