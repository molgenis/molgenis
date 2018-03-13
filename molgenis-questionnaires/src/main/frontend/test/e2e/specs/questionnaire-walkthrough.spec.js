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
    var firstColumnSelector = 'td:nth-child(1)'
    var secondColumnSelector = 'td:nth-child(2)'

    // ======= tests =======
    browser.expect.element(tableSelector).to.be.present
    browser.expect.element(firstRowSelector + ' > ' + firstColumnSelector).text.to.equal('Questionnaire #1')
    browser.expect.element(firstRowSelector + ' > ' + secondColumnSelector).text.to.equal('Not started yet')
  },

  'should load start page when button is clicked': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'load start page when button is clicked'

    // ===== selectors =====
    var firstRowViewButton = 'table > tbody > tr:nth-child(1) > td:nth-child(3) > a'
    var backToQuestionnaireListButton = 'a.btn.btn-sm.btn-outline-secondary.mt-2.router-link-active'
    var startQuestionnaireButton = 'a.btn.btn-lg.btn-primary.mt-2.float-right'
    var questionnaireLabelElement = 'h5'
    var questionnaireDescriptionElement = 'p'

    // ======= tests =======
    browser.click(firstRowViewButton)
    browser.expect.element(backToQuestionnaireListButton).to.be.present
    browser.expect.element(startQuestionnaireButton).to.be.present
    browser.expect.element(questionnaireLabelElement).to.be.present
    browser.expect.element(questionnaireDescriptionElement).to.be.present

    browser.expect.element(backToQuestionnaireListButton).text.to.equal('Back to questionnaire list')
    browser.expect.element(startQuestionnaireButton).text.to.equal('Start questionnaire')
    browser.expect.element(questionnaireLabelElement).text.to.equal('Questionnaire #1')
    browser.expect.element(questionnaireDescriptionElement).text.to.equal('<h3>This is the first mocked questionnaire response</h3><p>This is a not started questionnaire used for showing the chapters</p>')
  },

  'should load the first chapter when the start questionnaire button is clicked': function (browser) {
    // ======= setup =======
    browser.options.desiredCapabilities.name = 'load the first chapter when the start questionnaire button is clicked'

    // ===== selectors =====
    var firstRowViewButton = 'table > tbody > tr:nth-child(1) > td:nth-child(3) > a'
    var startQuestionnaireButton = 'a.btn.btn-lg.btn-primary.mt-2.float-right'
    var backToStartButton = 'div.card-header > div > div:nth-child(1) > a'
    var nextChapterButton = 'div.card-header > div > div:nth-child(3) > button'

    // ======= tests =======
    browser.click(firstRowViewButton)
    browser.click(startQuestionnaireButton)
  }
}
