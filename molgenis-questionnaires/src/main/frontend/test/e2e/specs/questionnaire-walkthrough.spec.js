/**
 * Due to long loading times, will not reset state of each
 * test but will continue from where the previous test ended
 *
 */
const backToStartButton = '#back-to-start-btn'
const nextChapterButton = '#next-chapter-btn'
const previousChapterButton = '#prev-chapter-btn'
const currentChapterSpan = '#current-chapter-label'
const submitButton = '#submit-questionnaire-btn'

module.exports = {
  before: function (browser) {
    browser.url(browser.globals.devServerURL)
  },

  after: function (browser) {
    browser.end()
  },

  'should render initial table content correctly': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'render initial table content correctly'

    // ===== selectors =====
    var tableSelector = 'table'
    var firstRowSelector = 'table > tbody > tr:nth-child(1)'
    var secondRowSelector = 'table > tbody > tr:nth-child(2)'
    var thirdRowSelector = 'table > tbody > tr:nth-child(3)'
    var firstColumnSelector = 'td:nth-child(1)'
    var secondColumnSelector = 'td:nth-child(2)'

    // ======= tests =======
    browser.expect.element(tableSelector).to.be.present

    browser.expect.element(firstRowSelector + ' > ' + firstColumnSelector).text.to.equal('Questionnaire #1')
    browser.expect.element(secondRowSelector + ' > ' + firstColumnSelector).text.to.equal('Questionnaire #2')
    browser.expect.element(thirdRowSelector + ' > ' + firstColumnSelector).text.to.equal('Questionnaire #3')
    browser.expect.element(firstRowSelector + ' > ' + secondColumnSelector).text.to.equal('Not started yet')
    browser.expect.element(secondRowSelector + ' > ' + secondColumnSelector).text.to.equal('Open')
    browser.expect.element(thirdRowSelector + ' > ' + secondColumnSelector).text.to.equal('Submitted')
  },

  'should load start page when button is clicked': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'load start page when button is clicked'

    // ===== selectors =====
    var firstRowViewButton = 'table > tbody > tr:nth-child(1) > td:nth-child(3) > a'
    var backToQuestionnairesListButton = 'a.btn.btn-sm.btn-outline-secondary.mt-2.router-link-active'
    var startQuestionnaireButton = 'body > div > div > div > button'
    var questionnaireLabelElement = 'h5'
    var questionnaireDescriptionElement = 'p'

    // ======= tests =======
    browser.click(firstRowViewButton)

    browser.expect.element(backToQuestionnairesListButton).to.be.present
    browser.expect.element(startQuestionnaireButton).to.be.present
    browser.expect.element(questionnaireLabelElement).to.be.present
    browser.expect.element(questionnaireDescriptionElement).to.be.present

    browser.expect.element(backToQuestionnairesListButton).text.to.equal('Back to questionnaires list')
    browser.expect.element(startQuestionnaireButton).text.to.equal('Start questionnaire')
    browser.expect.element(questionnaireLabelElement).text.to.equal('Questionnaire #1')
    browser.expect.element(questionnaireDescriptionElement).text.to.equal('This is the first mocked questionnaire response\n' +
      'This is a not started questionnaire used for showing the chapters')
  },

  'should load the first chapter when the start questionnaire button is clicked': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'load the first chapter when the start questionnaire button is clicked'

    // ===== selectors =====
    var startQuestionnaireButton = 'body > div > div > div > button'
    var chapterNavigationHeader = 'ul > a:nth-child(1)'
    var firstChapterListItem = 'ul > a:nth-child(2)'
    var secondChapterListItem = 'ul > a:nth-child(3)'

    // ======= tests =======
    browser.click(startQuestionnaireButton)

    browser.expect.element(backToStartButton).to.be.present
    browser.expect.element(nextChapterButton).to.be.present
    browser.expect.element(currentChapterSpan).to.be.present
    browser.expect.element(chapterNavigationHeader).to.be.present
    browser.expect.element(firstChapterListItem).to.be.present
    browser.expect.element(secondChapterListItem).to.be.present

    browser.expect.element(backToStartButton).text.to.equal('Back to start')
    browser.expect.element(nextChapterButton).text.to.equal('Next chapter')
    browser.expect.element(currentChapterSpan).text.to.equal('Chapter 1 of 2')
    browser.expect.element(chapterNavigationHeader).text.to.equal('Chapters')
    browser.expect.element(firstChapterListItem).text.to.equal('Chapter #1 - Personal information')
    browser.expect.element(secondChapterListItem).text.to.equal('Chapter #2 - Professional questions')
  },

  'should update progress in chapter navigation list when filling in questions': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'update progress in chapter navigation list when filling in questions'

    // ===== selectors =====
    var chapterOneNameQuestion = '#ch1_question1'
    var chapterOneAgeQuestion = '#ch1_question2'
    var chapterOneRequiredQuestion = '#ch1_question3'
    var chapterOneBoolQuestion = '#ch1_question4-0'
    var chapterOneProgressBar = 'ul > a:nth-child(2) > div > div'

    // ======= tests =======
    browser.expect.element(chapterOneProgressBar).to.have.attribute('aria-valuenow').which.contains('0')

    browser.setValue(chapterOneNameQuestion, 'Nightwatch test')
    browser.expect.element(chapterOneProgressBar).to.have.attribute('aria-valuenow').which.contains('25')

    browser.setValue(chapterOneAgeQuestion, 20)
    browser.expect.element(chapterOneProgressBar).to.have.attribute('aria-valuenow').which.contains('50')

    browser.setValue(chapterOneRequiredQuestion, 'A nightwatch generated text')
    browser.expect.element(chapterOneProgressBar).to.have.attribute('aria-valuenow').which.contains('75')

    browser.click(chapterOneBoolQuestion)
    browser.expect.element(chapterOneProgressBar).to.have.attribute('aria-valuenow').which.contains('100')

    browser.expect.element(chapterOneProgressBar).to.have.attribute('class').which.contains('bg-success')
  },

  'should navigate to the second chapter': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'navigate to the second chapter'

    // ===== selectors =====
    var currentChapterSpan = 'body > div > div > div > div:nth-child(2) > div.col-xs-12.col-sm-12.col-md-12.col-lg-9.col-xl-9 > div > div.card-header > div'

    // ======= tests =======
    browser.click(nextChapterButton)

    browser.expect.element(previousChapterButton).to.be.present
    browser.expect.element(currentChapterSpan).to.be.present
    browser.expect.element(submitButton).to.be.present

    browser.expect.element(previousChapterButton).text.to.equal('Previous chapter')
    browser.expect.element(currentChapterSpan).text.to.equal('Chapter 2 of 2')
    browser.expect.element(submitButton).text.to.equal('Submit')
  },

  'should finish questionnaire and submit': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'finish questionnaire and submit'

    // ===== selectors =====
    var vueCheckbox = '#ch2_question1-0'
    var javascriptCheckbox = '#ch2_question1-3'
    var molgenisWebsiteRadio = '#ch2_question2-0'
    var submissionText = 'p'
    var backToQuestionnairesListButton = 'body > div > div > div > a'
    var tableSelector = 'table'

    // ======= tests =======
    browser.click(vueCheckbox)
    browser.click(javascriptCheckbox)
    browser.click(molgenisWebsiteRadio)
    browser.click(submitButton)

    browser.expect.element(submissionText).to.be.present
    browser.expect.element(backToQuestionnairesListButton).to.be.present

    browser.expect.element(submissionText).text.to.equal('Thank you')
    browser.expect.element(backToQuestionnairesListButton).text.to.equal('Back to questionnaires list')

    browser.click(backToQuestionnairesListButton)
    browser.expect.element(tableSelector).to.be.present
  }
}
