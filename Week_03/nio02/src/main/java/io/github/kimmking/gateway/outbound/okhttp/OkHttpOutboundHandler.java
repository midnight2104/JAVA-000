package io.github.kimmking.gateway.outbound.okhttp;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;

import java.io.IOException;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class OkHttpOutboundHandler {
    private String backendUrl;
    private OkHttpClient okHttpClient;

    public OkHttpOutboundHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
        this.okHttpClient = new OkHttpClient();
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, List<HttpRequestFilter> filters) {
        final String url = this.backendUrl + fullRequest.uri();
        Request request = new Request.Builder().url(url).build();

        System.out.println(fullRequest.headers().get("hello"));

        //异步请求
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        System.out.println("请求失败");
                    }

                    @Override
                    public void onResponse(Response response) {
                        //请求成功的回调方法
                        handleResponse(fullRequest, ctx, response);
                    }
                });
    }

    private void handleResponse(final FullHttpRequest fullRequest,
                                final ChannelHandlerContext ctx,
                                final Response endpointResponse) {
        FullHttpResponse response = null;
        try {
            String body = endpointResponse.body().string();

           // System.out.println("请求：" + fullRequest.uri() + " ==> 响应结果：" + body);

            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body.getBytes()));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", Integer.parseInt(endpointResponse.header("Content-Length")));

           // System.out.println(endpointResponse.header("Content-Length"));

        } catch (Exception e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            exceptionCaught(ctx, e);
        } finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            }
            ctx.flush();
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
