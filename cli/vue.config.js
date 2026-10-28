const path = require('path')

// uni-app ships a patched @vue/component-compiler-utils (v3.1.0 fork) inside
// @dcloudio/vue-cli-plugin-uni. Its forked vue-loader resolves the bare module
// name, so without this alias the stock package (which does not emit
// `recyclableRender`) is used and template compilation fails.
require('module-alias').addAlias(
  '@vue/component-compiler-utils',
  path.resolve(__dirname, 'node_modules/@dcloudio/vue-cli-plugin-uni/packages/@vue/component-compiler-utils')
)

module.exports = {
  transpileDependencies: [],
  productionSourceMap: false,
  chainWebpack (config) {
    // 小于 4KB 的图片会被 url-loader 内联成 base64，导出的是 ES 模块对象
    // ({ default: 'data:...' })，而 uni-app 的 <image> 组件只接受字符串，
    // 结果是 $getRealPath(src) 里 src.indexOf 抛错、图片不显示。
    // 关掉内联，让所有图片都走文件 URL，导出统一是字符串。
    const images = config.module.rule('images')
    if (images && images.uses.has('url-loader')) {
      images.use('url-loader').tap(options => Object.assign({}, options, { limit: 0 }))
    }
  }
}
