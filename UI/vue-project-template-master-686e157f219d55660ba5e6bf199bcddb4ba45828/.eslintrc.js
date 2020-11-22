module.exports = {
    root: true,
    env: {
        node: true
    },
    'extends': [
        'plugin:vue/essential',
        '@vue/standard'
    ],
    rules: {
        'no-console': 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
        // allow async-await
        'generator-star-spacing': 'off',
        'no-extend-native': 0,//禁止扩展native对象
        'no-callback-literal': 'off',
// Default first line indentation 4
        // 'vue/script-indent': ['error', 4, {'baseIndent': 1}],
        'indent': ['error', 4, { 'SwitchCase': 1 }],
        'object-curly-spacing': 0
    },
    parserOptions: {
        parser: 'babel-eslint'
    },
    overrides: [
        {
            files: [
                '**/__tests__/*.{j,t}s?(x)',
                '**/tests/unit/**/*.spec.{j,t}s?(x)'
            ],
            env: {
                jest: true
            }
        }
    ]
}
