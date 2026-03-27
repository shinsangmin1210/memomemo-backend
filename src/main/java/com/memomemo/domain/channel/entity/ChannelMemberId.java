package com.memomemo.domain.channel.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChannelMemberId implements Serializable {

    private Long channel;
    private Long user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelMemberId that)) return false;
        return Objects.equals(channel, that.channel) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, user);
    }
}
