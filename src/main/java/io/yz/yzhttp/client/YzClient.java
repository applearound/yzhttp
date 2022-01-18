package io.yz.yzhttp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import io.yz.yzhttp.client.low.HttpObjectHandler;

public class YzClient {
    private final EventLoopGroup loop;

    public YzClient() {
        loop = new NioEventLoopGroup();
    }

    public void makeRequest(final HttpRequest httpRequest) {
        new Bootstrap()
                .group(loop)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        final ChannelPipeline pipeline = socketChannel.pipeline();

                        pipeline.addLast(new HttpObjectHandler());
                    }
                })
                .connect(httpRequest.host(), httpRequest.port())
                .addListener((GenericFutureListener<ChannelFuture>) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        System.out.println("Success");

                        final Channel channel = channelFuture.channel();
                        channel.writeAndFlush(httpRequest.cache())
                                .addListener((GenericFutureListener<ChannelPromise>) channelPromise -> {
                                    if (channelPromise.isSuccess()) {
                                        System.out.println("Write Success");
                                    } else {
                                        System.out.println("Write Failed");
                                    }
                                });
                    } else {
                        System.out.println("Failed");
                    }
                });
    }
}
