"use client";

import { useMemo, useState } from "react";

const colors = [
  { name: "RED", value: "#e95656" },
  { name: "BLUE", value: "#4c78e8" },
  { name: "GREEN", value: "#36a86b" },
  { name: "AMBER", value: "#e6a82d" },
];

function seededRound(round: number) {
  const word = colors[(round * 3 + 1) % colors.length];
  const ink = colors[(round * 5 + 2) % colors.length];
  return { word, ink };
}

export default function Home() {
  const [round, setRound] = useState(0);
  const [score, setScore] = useState(0);
  const [started, setStarted] = useState(false);
  const [finished, setFinished] = useState(false);
  const challenge = useMemo(() => seededRound(round), [round]);

  function choose(name: string) {
    if (name === challenge.ink.name) setScore((value) => value + 1);
    if (round >= 4) setFinished(true);
    else setRound((value) => value + 1);
  }

  function reset() {
    setRound(0); setScore(0); setFinished(false); setStarted(true);
  }

  return (
    <main>
      <nav className="nav">
        <a className="brand" href="#top" aria-label="Focus Reset home"><span className="mark">+</span> Focus Reset</a>
        <a className="nav-link" href="#science">How it works</a>
      </nav>

      <section className="hero" id="top">
        <div className="hero-copy">
          <p className="eyebrow">A private challenge from your friend</p>
          <h1>Your attention isn’t broken. <em>It’s been trained to scroll.</em></h1>
          <p className="lede">Take the five-round focus check. Then join a finite 7-day reset built around mind games—not another feed.</p>
          <div className="facts"><span>5 minutes daily</span><span>No ads</span><span>No public feed</span></div>
        </div>

        <section className="game-card" aria-labelledby="demo-title">
          <div className="card-top"><span>COLOR CLASH</span><span>{finished ? "COMPLETE" : `${round + 1} / 5`}</span></div>
          {!started ? (
            <div className="game-start">
              <div className="target-ring"><span>+</span></div>
              <h2 id="demo-title">Can your focus survive 60 seconds?</h2>
              <p>Ignore the word. Choose the color of its ink.</p>
              <button className="primary" onClick={() => setStarted(true)}>Try the focus check</button>
            </div>
          ) : finished ? (
            <div className="game-start result">
              <p className="eyebrow">DEMO RESULT</p>
              <strong>{score + (challenge.ink.name === challenge.word.name ? 1 : 0)}/5</strong>
              <h2>That was game performance—not a diagnosis.</h2>
              <p>The Android app adds Memory Grid, Signal Watch, Rule Shift, Story Recall, and private squad leaderboards.</p>
              <button className="primary" onClick={reset}>Play again</button>
              <a className="store-link" href="#join">Join the 7-day reset →</a>
            </div>
          ) : (
            <div className="game-live">
              <p>Choose the <strong>INK</strong> color</p>
              <div className="color-word" style={{ color: challenge.ink.value }}>{challenge.word.name}</div>
              <div className="choices">
                {colors.map((color) => <button key={color.name} onClick={() => choose(color.name)}>{color.name}</button>)}
              </div>
              <div className="mini-score">Correct so far: {score}</div>
            </div>
          )}
        </section>
      </section>

      <section className="challenge-strip" id="join">
        <div><p className="eyebrow">7-DAY NO-REELS RESET</p><h2>Reduce. Replace. Reclaim.</h2></div>
        <ol><li><b>01</b> Set a realistic app budget</li><li><b>02</b> Complete one daily Focus Run</li><li><b>03</b> Compare scores, not private screen time</li></ol>
        <a className="primary link-button" href="https://play.google.com/store/apps/details?id=com.focusreset.app">Get the Android app</a>
      </section>

      <section className="science" id="science">
        <p className="eyebrow">DESIGNED TO END</p>
        <h2>No infinite feed. No autoplay. No fake medical score.</h2>
        <div className="principles">
          <article><span>01</span><h3>Finite sessions</h3><p>One scored five-minute run each day, followed by a clear stopping point.</p></article>
          <article><span>02</span><h3>Five deliberate games</h3><p>Response control, working memory, sustained attention, rule switching, and recall.</p></article>
          <article><span>03</span><h3>Privacy by default</h3><p>Detailed app usage remains on the device. Squads see only scores and voluntary milestones.</p></article>
        </div>
      </section>

      <footer><span>Focus Reset · Working title</span><span>For ages 13+ · Not medical treatment</span></footer>
    </main>
  );
}
