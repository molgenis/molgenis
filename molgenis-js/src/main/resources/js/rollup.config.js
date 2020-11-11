import { nodeResolve } from '@rollup/plugin-node-resolve';
import commonjs from 'rollup-plugin-commonjs';
import { uglify } from "rollup-plugin-uglify";

export default {
  input: 'script-evaluator.js',
  output: {
    file: 'magma.js',
    format: 'iife',
    compact: true,
    name: "MagmaScript"
  },
  plugins: [nodeResolve(), commonjs(), uglify()],
};