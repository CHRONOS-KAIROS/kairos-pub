module.exports = {
    root: true,
    env: {
      node: true
    },
    extends: ["plugin:vue/essential", "@vue/typescript"],
    rules: {
      "no-console": "off",
      "no-debugger": process.env.NODE_ENV === "production" ? "error" : "off"
    },
    parserOptions: {
      parser: "@typescript-eslint/parser"
    },
    overrides: [
      {
        files: ["**/__tests__/*.{j,t}s?(x)"],
        env: {
          mocha: true
        } 
      }
    ]
  };
  