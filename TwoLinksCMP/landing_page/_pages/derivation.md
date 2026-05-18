---
layout: page
title: Derivation
include_in_header: true
---

# Equations of Motion for a Double Pendulum

TwoLinks simulates a **double pendulum** — a classic two-link mechanism that is one of the simplest physical systems to exhibit fully chaotic behavior. The notebook below documents the complete analytical derivation and numerical simulation that powers the app's real-time physics engine, and is intended for students, engineers, and scientists interested in nonlinear dynamics and multibody mechanics.

## What's Inside

The derivation uses **Kane's Method**, implemented symbolically in Python with `sympy.physics.mechanics`. Starting from the kinematics of two rigid links connected by revolute joints, it constructs the generalized equations of motion in terms of joint angles and angular rates. The resulting nonlinear ordinary differential equations are then integrated numerically using an adaptive **Runge-Kutta 4/5 (RK45)** solver from `scipy`, producing time histories of angles, rates, and end-effector positions.

The same mathematical model — compiled to Kotlin — runs in real time inside the TwoLinks app, available on **iPhone** and **iPad** (iOS), **Android**, and the **web**. On supported devices, the simulation can be placed directly into your physical environment using **Augmented Reality**: ARKit and RealityKit on iOS, and ARCore on Android, for a hands-on look at chaotic motion in spatial reality. The app is built with **Kotlin Multiplatform**, sharing a single physics core across all three platforms.

The trajectory plot of the pendulum tip — generated at the end of this notebook — is also the basis for the TwoLinks app icon.

---

<iframe src="{{ '/assets/derivation.html' | relative_url }}"
        style="width:100%; height:85vh; border:none; display:block;">
</iframe>
