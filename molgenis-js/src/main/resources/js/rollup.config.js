import { nodeResolve } from '@rollup/plugin-node-resolve';
import commonjs from 'rollup-plugin-commonjs';
import { uglify } from "rollup-plugin-uglify";

export default {
  input: 'magma.js',
  output: {
    file: 'script-evaluator.js',
    format: 'iife',
    compact: true,
    name: "MagmaScript"
  },
  plugins: [nodeResolve(), commonjs(), uglify()],
};