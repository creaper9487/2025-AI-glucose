/** @type {import('tailwindcss').Config} */
export default {
  content: [ './src/**/*.{html,js,ts,jsx,tsx,vue}', './index.html' ],
  theme: {
    extend: {
      fontFamily:{
        electro:["Electrolize", 'sans-serif'],
      },
      colors: {
        "greenScreen": "#00FF00",
      }
    },
  },
  plugins: [],
}

