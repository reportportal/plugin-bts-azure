const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const ESLintWebpackPlugin = require('eslint-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const pjson = require('./package.json');

const config = {
  entry: './src',
  output: {
    path: path.resolve(__dirname, 'build/public'), // compile to 'build/public' to allow public access to plugin resources
    filename: '[name].app.[contenthash:8].js',
    publicPath: 'auto',
    clean: true,
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        use: 'babel-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.svg$/,
        loader: 'svg-inline-loader',
      },
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'],
    alias: {
      components: path.resolve(__dirname, 'src/components'),
    },
  },
  externals: ['redux'],
  plugins: [
    new ESLintWebpackPlugin({
      context: './src',
      extensions: ['.js', '.jsx'],
      threads: true,
    }),
    new ModuleFederationPlugin({
      name: 'Azure_DevOps',
      filename: `remoteEntity.js`,
      shared: {
        react: {
          import: 'react',
          shareKey: 'react',
          shareScope: 'default',
          singleton: true,
          requiredVersion: pjson.dependencies.react,
        },
        'react-dom': {
          singleton: true,
          requiredVersion: pjson.dependencies['react-dom'],
        },
        'react-redux': {
          singleton: true,
          requiredVersion: pjson.dependencies['react-redux'],
        },
      },
      exposes: {
        './integrationSettings': './src/components/integrationSettings',
        './integrationFormFields': './src/components/integrationFormFields',
      },
    }),
    new CopyPlugin({
      patterns: [
        { from: path.resolve(__dirname, './src/metadata.json') },
        { from: path.resolve(__dirname, './src/plugin-icon.svg') },
      ],
    }),
  ],
};

module.exports = config;
