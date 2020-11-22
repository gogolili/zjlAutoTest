# vue-project-template

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run dev
```

### Compiles and minifies for production
```
npm run build
```

# 功能展示
- [x] 本地代理
- [x] 全局变量引入
- [x] SCSS支持
- [x] standard-eslint 规范代码
- [x] mock 数据
- [x] babel转义
- [x] 全局 http 请求的简单封装

#### 原有的 vue-cli2模板请见release-cli2分支

## Git 规范

使用 [commitlint](https://github.com/conventional-changelog/commitlint) 工具，常用有以下几种类型：

- feat ：新功能
- fix ：修复 bug
- chore ：对构建或者辅助工具的更改
- refactor ：既不是修复 bug 也不是添加新功能的代码更改
- style ：不影响代码含义的更改 (例如空格、格式化、少了分号)
- docs ：只是文档的更改
- perf ：提高性能的代码更改
- revert ：撤回提交
- test ：添加或修正测试
- merge ：merge代码、解决冲突

举例

git commit -m 'feat: add list'

git commit -m 'merge: conflict resolution'
