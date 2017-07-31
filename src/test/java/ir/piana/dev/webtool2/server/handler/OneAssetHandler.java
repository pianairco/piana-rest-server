package ir.piana.dev.webtool2.server.handler;

import ir.piana.dev.webtool2.server.annotation.AssetHandler;
import ir.piana.dev.webtool2.server.annotation.Handler;
import ir.piana.dev.webtool2.server.annotation.HandlerType;
import ir.piana.dev.webtool2.server.role.RoleType;

/**
 * Created by SYSTEM on 7/29/2017.
 */
@Handler(baseUrl = "", handlerType = HandlerType.ASSET_HANDLER)
@AssetHandler(assetPath = "./html-root", sync = false, requiredRole = RoleType.GUEST)
public class OneAssetHandler {
}
