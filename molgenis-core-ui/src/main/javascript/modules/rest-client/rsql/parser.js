import peg from "pegjs";
import grammar from "./grammar";

/**
 * Exports a parser for the rsql grammar
 *
 * import parser from 'parser'
 *
 * <code>parser.parse("xbool==false")</code> returns
 * <code>{
        "selector": "xbool",
        "comparison": "==",
        "arguments": "false"
    }</code>
 */
export default peg.generate(grammar)

