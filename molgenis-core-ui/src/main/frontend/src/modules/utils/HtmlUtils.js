export function htmlEscape(string) {
    var entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        "\u2264": "&lte;",
        ">": "&gt;",
        "\u2265": "&gte;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
    };

    return String(string).replace(/[&<>"'\/]/g, function (s) {
        return entityMap[s];
    });
};
