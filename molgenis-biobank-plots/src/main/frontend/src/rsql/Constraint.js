// @flow

export type Constraint = SimpleConstraint | ComplexConstraint

export class ComplexConstraint {
  operator: "AND" | "OR"
  operands: Array<Constraint>

  constructor (operator: "AND" | "OR", operands: Array<Constraint>) {
    this.operator = operator
    this.operands = operands
  }
}

export class SimpleConstraint {
  selector: string
  comparison: "==" | "lt" | "gt" | "=q="
  arguments: string|number|boolean|Array<string|number|boolean>

  constructor (selector: string,
              comparison: "==" | "lt" | "gt" | "=q=",
              args: string|number|boolean|Array<string|number|boolean>) {
    this.selector = selector
    this.comparison = comparison
    this.arguments = args
  }
}
