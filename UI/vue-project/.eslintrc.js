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
//         'no-console': 'off',
//         'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
//         // allow async-await
//         'generator-star-spacing': 'off',
//         'no-extend-native': 0,//禁止扩展native对象
//         'no-callback-literal': 'off',
// // Default first line indentation 4
//         // 'vue/script-indent': ['error', 4, {'baseIndent': 1}],
//         'indent': ['error', 0, {'SwitchCase': 0}],
//         'object-curly-spacing': 0
    },
    'overrides': [// remove rslint indent rules
        {
            'files': ['*.vue'],
            'rules': {
                'indent': 'off'
            }
        }
    ],
    parserOptions: {
        parser: 'babel-eslint'
    }
}
