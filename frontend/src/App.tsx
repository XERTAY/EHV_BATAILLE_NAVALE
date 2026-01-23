import { useState } from 'react'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="App">
      <h1>🚢 Bataille Navale</h1>
      <p>Bienvenue dans le jeu de Bataille Navale</p>
    </div>
  )
}

export default App

