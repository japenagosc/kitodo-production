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

package org.kitodo.services;

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.config.ConfigMain;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.RulesetDAO;
import org.kitodo.data.index.Indexer;
import org.kitodo.data.index.elasticsearch.type.RulesetType;

import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

public class RulesetService {

    private static final Logger logger = Logger.getLogger(RulesetService.class);

    private RulesetDAO rulesetDao = new RulesetDAO();
    private RulesetType rulesetType = new RulesetType();
    private Indexer<Ruleset, RulesetType> indexer = new Indexer<>("kitodo", Ruleset.class);

    /**
     * Method saves object to database and insert document to the index of Elastic Search.
     *
     * @param ruleset object
     */
    public void save(Ruleset ruleset) throws DAOException, IOException {
        rulesetDao.save(ruleset);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(ruleset, rulesetType);
    }

    public Ruleset find(Integer id) throws DAOException {
        return rulesetDao.find(id);
    }

    public List<Ruleset> findAll() throws DAOException {
        return rulesetDao.findAll();
    }

    public List<Ruleset> search(String query) throws DAOException {
        return rulesetDao.search(query);
    }

    /**
     * Method removes object from database and document from the index of Elastic Search.
     *
     * @param ruleset object
     */
    public void remove(Ruleset ruleset) throws DAOException, IOException {
        rulesetDao.remove(ruleset);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(ruleset, rulesetType);
    }

    public void remove(Integer id) throws DAOException {
        rulesetDao.remove(id);
    }

    /**
     * Get preferences.
     *
     * @param ruleset object
     * @return preferences
     */
    public Prefs getPreferences(Ruleset ruleset) {
        Prefs myPreferences = new Prefs();
        try {
            myPreferences.loadPrefs(ConfigMain.getParameter("RegelsaetzeVerzeichnis")
                    + ruleset.getFile());
        } catch (PreferencesException e) {
            logger.error(e);
        }
        return myPreferences;
    }
}
