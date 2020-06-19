const PLUGIN_URI = 'systems.dmx.images'

const { CleanWebpackPlugin } = require('clean-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const { VueLoaderPlugin }  = require('vue-loader')

module.exports = {
  entry: './src/main/js/plugin.js',
  output: {
    path: __dirname + '/src/main/resources/web',
    filename: '[chunkhash].plugin.js',
    chunkFilename: '[chunkhash].[name].js',
    publicPath: '/' + PLUGIN_URI + '/',
    library: '_' + PLUGIN_URI.replace(/[.-]/g, '_'),
    libraryTarget: 'jsonp'
  },
  resolve: {
    extensions: [".js", ".vue"]
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader'
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: /node_modules/
      },
      {
        test: /\.css$/,
        loader: [MiniCssExtractPlugin.loader, 'css-loader']
      }
    ]
  },
  plugins: [
    new CleanWebpackPlugin(),
    new VueLoaderPlugin(),
	  new MiniCssExtractPlugin({
      filename: '[contenthash].style.css',
      chunkFilename: '[chunkhash].[name].css',
    })
  ],
  stats: {
    entrypoints: false
  }
}
