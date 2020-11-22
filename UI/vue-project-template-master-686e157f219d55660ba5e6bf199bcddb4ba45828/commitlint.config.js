/**
 * Created by gaoguoqing on 7/2/20.
 *
 */
module.exports = {
    extends: ['@commitlint/config-conventional'],
    rules: {
        'type-enum': [2, 'always', [
            'chore',
            'docs',
            'feat',
            'fix',
            'perf',
            'refactor',
            'revert',
            'style',
            'test',
            'merge'
        ]]
        // 'subject-full-stop': [0, 'never'],
        // 'subject-case': [0, 'never']
    }
}
