/*
 * This file is part of HuskChat, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskchat.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.william278.huskchat.HuskChat;
import net.william278.huskchat.channel.Channel;
import net.william278.huskchat.message.ChatMessage;
import net.william278.huskchat.user.VelocityUser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VelocityListener extends PlayerListener {

    public VelocityListener(@NotNull HuskChat plugin) {
        super(plugin);
    }

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerChat(PlayerChatEvent e) {
        if (!e.getResult().isAllowed()) {
            return;
        }

        // Verify they are in a channel
        final VelocityUser player = VelocityUser.adapt(e.getPlayer(), plugin);
        final Optional<Channel> channel = plugin.getChannels().getChannel(
                plugin.getUserCache().getPlayerChannel(player.getUuid())
        );
        if (channel.isEmpty()) {
            plugin.getLocales().sendMessage(player, "error_no_channel");
            return;
        }

        // Send the chat message, determine if the event should be canceled
        if (new ChatMessage(channel.get(), player, e.getMessage(), plugin).dispatch()) {
            e.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }

    @Subscribe
    public void onPlayerChangeServer(ServerConnectedEvent e) {
        final String server = e.getServer().getServerInfo().getName();
        final VelocityUser player = VelocityUser.adapt(e.getPlayer(), plugin);
        handlePlayerSwitchServer(player, server);
    }

    @Subscribe
    public void onPlayerJoinNetwork(PostLoginEvent e) {
        handlePlayerJoin(VelocityUser.adapt(e.getPlayer(), plugin));
    }

    @Subscribe
    public void onPlayerQuitNetwork(DisconnectEvent e) {
        handlePlayerQuit(VelocityUser.adapt(e.getPlayer(), plugin));
    }

}
