/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

class ConfigOpac {
    private static XMLConfiguration config;
    private static final Logger logger = Logger.getLogger(ConfigOpac.class);

    /**
     * Returns the XMLConfiguration of the plugin containing docType names and
     * conditions for structureType classification.
     *
     * @return config the XMLConfiguration of the plugin
     */
    protected static XMLConfiguration getConfig() {

        if (config != null) {
            logger.trace("Using already loaded configuration.");
            return config;
        }

        String configPfad = FilenameUtils.concat(ModsPlugin.getConfigDir(), ModsPlugin.OPAC_CONFIGURATION_FILE);
        if (!new File(configPfad).exists()) {
            String message = "File not found: ".concat(configPfad);
            throw new RuntimeException(message, new FileNotFoundException(message));
        }

        try {
            config = new XMLConfiguration(configPfad);
        } catch (ConfigurationException e) {
            logger.error("Configuration error inside file " + configPfad + ". Will continue with empty configuration. Errors may appear.");
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());

        return config;
    }

    static List<String> getAllCatalogues() {
        List<String> catalogueTitles = new ArrayList<String>();
        // can not use class variable as method is called by reflection
        XMLConfiguration conf = getConfig();
        for (int i = 0; i <= conf.getMaxIndex("catalogue"); i++) {
            catalogueTitles.add(conf.getString("catalogue(" + i + ")[@title]"));
        }
        return catalogueTitles;
    }

    /**
     * find Catalogue in Opac-Configurationlist
     * ================================================================
     */
    static ConfigOpacCatalogue getCatalogueByName(String inTitle) {
        // can not use class variable as method is called by reflection
        XMLConfiguration conf = getConfig();
        int countCatalogues = conf.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String scheme = "http";
            String path = "/sru?version=1.2";
            int port = 80;
            String title = conf.getString("catalogue(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                String description = conf.getString("catalogue(" + i + ").config[@description]");
                String address = conf.getString("catalogue(" + i + ").config[@address]");
                String opacType = conf.getString("catalogue(" + i + ").config[@opacType]", ModsPlugin.MODS_STRING);

                if (conf.getString("catalogue(" + i + ").config[@scheme]") != null) {
                    scheme = conf.getString("catalogue(" + i + ").config[@scheme]");
                }

                if (conf.getString("catalogue(" + i + ").config[@port]") != null) {
                    port = Integer.parseInt(conf.getString("catalogue(" + i + ").config[@port]"));
                }

                if (conf.getString("catalogue(" + i + ").config[@path]") != null) {
                    path = conf.getString("catalogue(" + i + ").config[@path]");
                }
                path = path + "&";

                ConfigOpacCatalogue coc = new ConfigOpacCatalogue(title, description, address, opacType, scheme, path, port);
                return coc;
            }
        }
        return null;
    }

    /**
     * return all configured Doctype-Titles from Configfile
     * ================================================================
     */
    private static ArrayList<String> getAllDoctypeTitles() {
        ArrayList<String> myList = new ArrayList<String>();
        // can not use class variable as method is called by reflection
        XMLConfiguration conf = getConfig();
        int countTypes = conf.getMaxIndex("doctypes.type");
        for (int i = 0; i <= countTypes; i++) {
            String title = conf.getString("doctypes.type(" + i + ")[@title]");
            myList.add(title);
        }
        return myList;
    }

    /**
     * return all configured Doctype-Titles from Configfile
     * ================================================================
     */
    static ArrayList<ConfigOpacDoctype> getAllDoctypes() {
        ArrayList<ConfigOpacDoctype> myList = new ArrayList<ConfigOpacDoctype>();
        for (String title : getAllDoctypeTitles()) {
            myList.add(getDoctypeByName(title));
        }
        return myList;
    }

    /**
     * get doctype from mapping of opac response first check if there is a
     * special mapping for this
     * ================================================================
     */
    static ConfigOpacDoctype getDoctypeByMapping(String inMapping, String inCatalogue) {
        // can not use class variable as method is called by reflection
        XMLConfiguration conf = getConfig();
        int countCatalogues = conf.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = conf.getString("catalogue(" + i + ")[@title]");
            if (title.equals(inCatalogue)) {

                // alle speziell gemappten DocTypes eines Kataloges einlesen

                HashMap<String, String> labels = new HashMap<String, String>();
                int countLabels = conf.getMaxIndex("catalogue(" + i + ").specialmapping");
                for (int j = 0; j <= countLabels; j++) {
                    String type = conf.getString("catalogue(" + i + ").specialmapping[@type]");
                    String value = conf.getString("catalogue(" + i + ").specialmapping");
                    labels.put(value, type);
                }
                if (labels.containsKey(inMapping)) {
                    return getDoctypeByName(labels.get(inMapping));
                }
            }
        }

        // falls der Katalog kein spezielles Mapping für den Doctype hat, jetzt
        // in den Doctypes suchen

        for (String title : getAllDoctypeTitles()) {
            ConfigOpacDoctype tempType = getDoctypeByName(title);
            if (tempType.getMappings().contains(inMapping)) {
                return tempType;
            }
        }
        return null;
    }

    /**
     * get doctype from title
     * ================================================================
     */
    @SuppressWarnings("unchecked")
    private static ConfigOpacDoctype getDoctypeByName(String inTitle) {
        // can not use class variable as method is called by reflection
        XMLConfiguration conf = getConfig();
        // TODO: can't we get elements by attribute value directly here (instead
        // of iterating over all doctypes and always comparing the title
        // attribute with the given string manually?)
        // should make full use of proper XPath here!
        int countCatalogues = conf.getMaxIndex("doctypes.type");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = conf.getString("doctypes.type(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                /* Sprachen erfassen */
                HashMap<String, String> labels = new HashMap<String, String>();
                int countLabels = conf.getMaxIndex("doctypes.type(" + i + ").label");
                for (int j = 0; j <= countLabels; j++) {
                    String language = conf.getString("doctypes.type(" + i + ").label(" + j + ")[@language]");
                    String value = conf.getString("doctypes.type(" + i + ").label(" + j + ")");
                    labels.put(language, value);
                }
                boolean periodical;
                boolean multiVolume;
                boolean containedWork;

                try {
                    periodical = conf.getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
                } catch (NoSuchElementException noParameterIsNewspaper) {
                    periodical = false;
                }

                try {
                    multiVolume = conf.getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
                } catch (NoSuchElementException noParameterIsNewspaper) {
                    multiVolume = false;
                }

                try {
                    containedWork = conf.getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
                } catch (NoSuchElementException noParameterIsNewspaper) {
                    containedWork = false;
                }

                ArrayList<String> mappings = (ArrayList<String>) conf.getList("doctypes.type(" + i + ").mapping");

                ConfigOpacDoctype cod = new ConfigOpacDoctype(title, periodical, multiVolume, containedWork, mappings);
                return cod;
            }
        }
        return null;
    }
}
