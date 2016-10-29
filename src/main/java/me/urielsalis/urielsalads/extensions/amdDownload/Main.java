package me.urielsalis.urielsalads.extensions.amdDownload;

import me.urielsalis.urielsalads.extensions.ExtensionAPI;
import me.urielsalis.urielsalads.extensions.download.AMD;
import me.urielsalis.urielsalads.extensions.download.Config;
import net.engio.mbassy.listener.Handler;
import nu.xom.*;

import java.io.IOException;

import static me.urielsalis.urielsalads.extensions.download.DownloadMain.*;

/**
 * UrielSalads
 * Copyright (C) 2016 Uriel Salischiker
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@ExtensionAPI.Extension(name = "amd-download", version = "1.0.0", dependencies = {"download", "irc"}, id = "amd-download/1.0.0")
public class Main {
    @ExtensionAPI.ExtensionInit("amd-download/1.0.0")
    public static void initAMDDownloader(ExtensionAPI api) {
        try {
            api.registerListener("commandEvent", new CommandListener());
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
        if(getConfig().amd.newConfig) {
            fullUpdate();
            config.amd.newConfig = false;
            writeJSON();
        }
    }

    private static void fullUpdate() {
        try {
            Document document = new Builder().build("http://support.amd.com/drivers/xml/driver_selector_09_us.xml");
            Element root = document.getRootElement();
            Elements platforms = root.getChildElements("platform");
            for (int i = 0; i < platforms.size(); i++) {
                Element platformElement = platforms.get(i);
                String platformName = platformElement.getAttributeValue("name");
                String platformID = platformElement.getAttributeValue("value");
                AMD.Platform platform = new AMD.Platform(platformName, platformID);
                Elements productFamilies = platformElement.getChildElements("productfamily");
                for (int j = 0; j < productFamilies.size(); j++) {
                    Element productFamilyElement = productFamilies.get(j);
                    String productFamilyName = productFamilyElement.getAttributeValue("name");
                    String productFamilyID = productFamilyElement.getAttributeValue("value");
                    if(productFamilyID.equals("autodetect")) continue;
                    AMD.Platform.ProductFamily productFamily = new AMD.Platform.ProductFamily(productFamilyName, productFamilyID);
                    Elements products = productFamilyElement.getChildElements("product");
                    for (int k = 0; k < products.size(); k++) {
                        Element productElement = products.get(k);
                        String productName = productElement.getAttributeValue("label");
                        String productID = productElement.getAttributeValue("value");
                        if(productID.equals("autodetect") || productID.equals("not_sure")) continue;
                        AMD.Platform.ProductFamily.Product product = new AMD.Platform.ProductFamily.Product(productName, productID);
                        Config.GPU gpu = new Config.GPU(productName);
                        Elements versions = productElement.getChildElements("version");
                        for (int l = 0; l < versions.size(); l++) {
                            Element versionElement = versions.get(l);
                            String type = versionElement.getAttributeValue("type");
                            String number = versionElement.getAttributeValue("number"); //version
                            Elements downloads = versionElement.getChildElements();
                            AMD.Platform.ProductFamily.Product.Version version = new AMD.Platform.ProductFamily.Product.Version(type, number, downloads);
                            if(version.shouldDownload) {
                                product.versions.add(version);
                                gpu.addAMD(version);
                            }
                        }
                        productFamily.products.add(product);
                        config.list.add(gpu);
                    }
                    platform.productFamilies.add(productFamily);
                }
                config.amd.platforms.add(platform);
            }
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
        }

    }

    @ExtensionAPI.ExtensionUnload("amd-download/1.0.0")
    public static void unloadAMDDownloader(ExtensionAPI api) {
        try {
            api.unregisterListener("commandEvent", "amd-download/1.0.0/CommandListener");
        } catch (ExtensionAPI.EventDoesntExistsException e) {
            e.printStackTrace();
        }
    }

    private static class CommandListener implements ExtensionAPI.Listener {
        @Handler
        public static void handle(me.urielsalis.urielsalads.extensions.irc.Main.Command command) {
            if(command.getName().equals("amdFullUpdate")) {
                fullUpdate();
                writeJSON();
            }
        }

        @Override
        public String name() {
            return "amd-download/1.0.0/CommandListener";
        }
    }

}
