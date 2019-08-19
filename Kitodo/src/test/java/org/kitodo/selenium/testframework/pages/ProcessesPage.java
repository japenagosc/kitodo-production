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

package org.kitodo.selenium.testframework.pages;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.production.helper.Helper;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class ProcessesPage extends Page<ProcessesPage> {

    private static final String PROCESSES_TAB_VIEW = "processesTabView";
    private static final String PROCESSES_FORM = PROCESSES_TAB_VIEW + ":processesForm";
    private static final String BATCH_FORM = PROCESSES_TAB_VIEW + ":batchForm";
    private static final String PROCESSES_TABLE = PROCESSES_FORM + ":processesTable";
    private static final String PROCESS_TITLE = "Second process";

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TAB_VIEW)
    private WebElement processesTabView;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_TABLE + "_data")
    private WebElement processesTable;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":selectBatches")
    private WebElement batchesSelect;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":selectProcesses")
    private WebElement processesSelect;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/processEdit.jsf?referer=processes&id=1']")
    private WebElement editProcessLink;

    private WebElement downloadDocketLink;

    private WebElement downloadLogLink;

    private WebElement editMetadataLink;

    @SuppressWarnings("unused")
    @FindBy(id = "search")
    private WebElement searchForProcessesButton;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_FORM + ":createExcel")
    private WebElement downloadSearchResultAsExcel;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_FORM + ":createPdf")
    private WebElement downloadSearchResultAsPdf;

    @SuppressWarnings("unused")
    @FindBy(id = PROCESSES_FORM + ":actionsButton")
    private WebElement actionsButton;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":createBatchSelection")
    private WebElement createBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":renameBatchSelection")
    private WebElement renameBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":deleteBatchSelection")
    private WebElement deleteBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":addProcessesToBatch")
    private WebElement addProcessesToBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":removeProcessesFromBatchSelection")
    private WebElement removeProcessesFromBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = BATCH_FORM + ":downloadDocket")
    private WebElement downloadDocketForBatchLink;

    @SuppressWarnings("unused")
    @FindBy(id = "createBatchForm:batchTitle")
    private WebElement createBatchTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "createBatchForm:save")
    private WebElement createBatchSaveButton;

    @SuppressWarnings("unused")
    @FindBy(id = "renameBatchForm:batchTitle")
    private WebElement renameBatchTitleInput;

    @SuppressWarnings("unused")
    @FindBy(id = "renameBatchForm:save")
    private WebElement renameBatchSaveButton;

    @FindBy(id = "processesTabView:batchForm:batchActionsButton")
    private WebElement batchActionsButton;

    @FindBy(id = "processesTabView:batchForm:processActionsButton")
    private WebElement processActionsButton;

    public ProcessesPage() {
        super("pages/processes.jsf");
    }

    /**
     * Goes to processes page.
     *
     * @return The processes page.
     */
    @Override
    public ProcessesPage goTo() throws Exception {
        Pages.getTopNavigation().gotoProcesses();
        await("Wait for execution of link click").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
        return this;
    }

    /**
     * Go to edit page for creating a new project.
     *
     * @return project edit page
     */
    public ProcessEditPage editProcess() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        clickButtonAndWaitForRedirect(editProcessLink, Pages.getProcessEditPage().getUrl());
        return Pages.getProcessEditPage();
    }

    public int countListedProcesses() throws Exception {
        if (!isAt()) {
            goTo();
        }
        return getRowsOfTable(processesTable).size();
    }

    public int countListedBatches() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());
        Select batchSelect = new Select(batchesSelect);
        return batchSelect.getOptions().size();
    }

    /**
     * Returns a list of all processes titles which were displayed on process page.
     *
     * @return list of processes titles
     */
    public List<String> getProcessTitles() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(processesTable, 1);
    }

    public void createNewBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select processSelect = new Select(processesSelect);
        processSelect.selectByIndex(0);
        processSelect.selectByIndex(1);
        processActionsButton.click();

        await("Wait for actions menu to open").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> createBatchLink.isDisplayed());
        createBatchLink.click();

        createBatchTitleInput.sendKeys("SeleniumBatch");
        createBatchSaveButton.click();
    }

    public void renameBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        batchesSelect = Browser.getDriver()
                .findElementById(BATCH_FORM + ":selectBatches");

        await("Wait for batch tab to open").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> batchesSelect.isDisplayed());
        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("First batch (1 Vorgänge)");

        processActionsButton = Browser.getDriver()
                .findElementById("processesTabView:batchForm:processActionsButton");

        await("Wait for process actions button to become clickable").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> processActionsButton.isDisplayed());
        processActionsButton.click();

        renameBatchLink = Browser.getDriver().findElementById(BATCH_FORM + ":renameBatchSelection");
        await("Wait for actions menu to open").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> renameBatchLink.isDisplayed());
        renameBatchLink.click();

        renameBatchTitleInput.sendKeys("SeleniumBatch");
        renameBatchSaveButton.click();
    }

    public void removeProcessFromBatch() throws Exception {

        switchToTabByIndex(TabIndex.BATCHES.getIndex());
        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("Third batch (2 Vorgänge)");

        Select processSelect = new Select(processesSelect);
        processSelect.selectByVisibleText("First process");

        processActionsButton.click();
        removeProcessesFromBatchLink.click();
    }

    public void deleteBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("Third batch (2 Vorgänge)");

        batchActionsButton.click();
        deleteBatchLink.click();
    }

    public void downloadDocketForBatch() throws Exception {
        switchToTabByIndex(TabIndex.BATCHES.getIndex());

        Select batchSelect = new Select(batchesSelect);
        batchSelect.selectByVisibleText("Third batch (2 Vorgänge)");

        downloadDocketForBatchLink.click();

        await("Wait for docket file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> isFileDownloaded.matches(new File(Browser.DOWNLOAD_DIR + PROCESS_TITLE + ".pdf")));
    }

    public void downloadDocket() {
        setDownloadDocketLink();
        downloadDocketLink.click();

        await("Wait for docket file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions().until(() -> isFileDownloaded.matches(
                    new File(Browser.DOWNLOAD_DIR + Helper.getNormalizedTitle(PROCESS_TITLE) + ".pdf")));
    }

    public void downloadLog() {
        setDownloadLogLink();
        downloadLogLink.click();

        await("Wait for log file download").pollDelay(700, TimeUnit.MILLISECONDS).atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> isFileDownloaded.matches(new File(KitodoConfig.getParameter(ParameterCore.DIR_USERS)
                        + "kowal/" + Helper.getNormalizedTitle(PROCESS_TITLE) + "_log.xml")));
    }

    public void editMetadata() throws IllegalAccessException, InstantiationException {
        setEditMetadataLink();
        clickButtonAndWaitForRedirect(editMetadataLink, Pages.getMetadataEditorPage().getUrl());
    }

    public void downloadSearchResultAsExcel() {
        actionsButton.click();
        await("Wait for actions menu to open").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> downloadSearchResultAsExcel.isDisplayed());
        downloadSearchResultAsExcel.click();

        await("Wait for search result excel file download").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isFileDownloaded.matches(new File(Browser.DOWNLOAD_DIR + "search.xls")));
    }

    public void downloadSearchResultAsPdf() {
        actionsButton.click();
        await("Wait for actions menu to open").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until(() -> downloadSearchResultAsPdf.isDisplayed());
        downloadSearchResultAsPdf.click();

        await("Wait for search result pdf file download").pollDelay(700, TimeUnit.MILLISECONDS)
                .atMost(30, TimeUnit.SECONDS).ignoreExceptions()
                .until(() -> isFileDownloaded.matches(new File(Browser.DOWNLOAD_DIR + "search.pdf")));
    }

    private void setDownloadDocketLink() {
        int index = getRowIndex(processesTable, PROCESS_TITLE);
        downloadDocketLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":downloadDocket");
    }

    private void setEditMetadataLink() {
        int index = getRowIndex(processesTable, PROCESS_TITLE);
        editMetadataLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":readXML");
    }

    private void setDownloadLogLink() {
        int index = getRowIndex(processesTable, PROCESS_TITLE);
        downloadLogLink = Browser.getDriver().findElementById(PROCESSES_TABLE + ":" + index + ":exportLogXml");
    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @param index
     *            of tab to be clicked
     */
    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, processesTabView);
    }

    public void navigateToExtendedSearch() throws IllegalAccessException, InstantiationException {
        clickButtonAndWaitForRedirect(searchForProcessesButton, Pages.getExtendedSearchPage().getUrl());
    }
}
