package com.zslt_official.zslt_eco.grpc.v1.service.impl;

import com.zslt_official.zslt_eco.dto.req.UserLoginReqDTO;
import com.zslt_official.zslt_eco.dto.req.RevokeTokenReqDTO;
import com.zslt_official.zslt_eco.dto.resp.UserLoginRespDTO;
import com.zslt_official.zslt_eco.grpc.v1.*;

import com.zslt_official.zslt_eco.service.TokenService;

import com.zslt_official.zslt_eco.common.enums.JwtEnum;
import com.zslt_official.zslt_eco.common.exception.TokenExpiredException;
import com.zslt_official.zslt_eco.common.exception.TokenInvalidException;
import com.zslt_official.zslt_eco.common.exception.TokenRevokedException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import io.jsonwebtoken.Claims;
import java.util.Map;

@GrpcService
public class AuthGrpcServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    @Autowired
    private TokenService tokenService;

    @Override
    public void getToken(GetTokenRequest request, StreamObserver<GetTokenResponse> responseObserver) {
        try {
            UserLoginReqDTO reqDTO = UserLoginReqDTO.builder()
                    .userId(request.getUserId())
                    .serverName(request.getServerName())
                    .build();

            UserLoginRespDTO respDTO = tokenService.getToken(reqDTO);

            GetTokenResponse response = GetTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(JwtEnum.SUCCESS_GET.getMessage())
                    .setAccessToken(respDTO.getAccessToken())
                    .setRefreshToken(respDTO.getRefreshToken())
                    .setUserId(respDTO.getUserId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            GetTokenResponse response = GetTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(JwtEnum.FAILED_GET.getMessage() + ": " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequest request, StreamObserver<RefreshTokenResponse> responseObserver) {
        try {
            if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
                RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage(JwtEnum.MISSING_TOKEN.getMessage())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            Map<String, String> tokens = tokenService.refreshToken(request.getRefreshToken());

            RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Token刷新成功")
                    .setAccessToken(tokens.get("access_token"))
                    .setRefreshToken(tokens.get("refresh_token"))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TokenRevokedException e) {
            RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(JwtEnum.REVOKED_TOKEN.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TokenExpiredException e) {
            RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(JwtEnum.EXPIRED_TOKEN.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TokenInvalidException e) {
            RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(JwtEnum.INVALID_TOKEN.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            RefreshTokenResponse response = RefreshTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Token刷新失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void revokeToken(RevokeTokenRequest request, StreamObserver<RevokeTokenResponse> responseObserver) {
        try {
            RevokeTokenReqDTO reqDTO = new RevokeTokenReqDTO();
            reqDTO.setUserId(request.getUserId());
            reqDTO.setServerName(request.getServerName());
            reqDTO.setTokenCategory(request.getTokenCategory());

            tokenService.revokeToken(reqDTO);

            RevokeTokenResponse response = RevokeTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Token撤销成功")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            RevokeTokenResponse response = RevokeTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Token撤销失败: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                        .setSuccess(false)
                        .setValid(false)
                        .setMessage(JwtEnum.MISSING_TOKEN.getMessage())
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            Claims claims = tokenService.validateToken(request.getToken(), request.getTokenCategory());

            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setValid(true)
                    .setMessage(JwtEnum.SUCCESS_VALIDATE.getMessage())
                    .setUserId(claims.get("user_id", String.class))
                    .setServerName(claims.get("server_name", String.class))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TokenRevokedException e) {
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setValid(false)
                    .setMessage(JwtEnum.REVOKED_TOKEN.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TokenExpiredException e) {
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setValid(false)
                    .setMessage(JwtEnum.EXPIRED_TOKEN.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TokenInvalidException e) {
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setSuccess(true)
                    .setValid(false)
                    .setMessage(JwtEnum.INVALID_TOKEN.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setSuccess(false)
                    .setValid(false)
                    .setMessage("Token验证失败: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
