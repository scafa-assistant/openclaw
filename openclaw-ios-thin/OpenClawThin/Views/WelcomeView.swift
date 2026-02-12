//
//  WelcomeView.swift
//  OpenClawThin
//
//  Begrüßungsbildschirm
//

import SwiftUI

struct WelcomeView: View {
    @State private var showSetup = false
    
    var body: some View {
        VStack(spacing: 40) {
            Spacer()
            
            // Logo
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [.blue, .purple],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 150, height: 150)
                    .shadow(radius: 20)
                
                Image(systemName: "waveform.circle.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 80, height: 80)
                    .foregroundColor(.white)
            }
            
            // Title
            VStack(spacing: 12) {
                Text("OpenClaw")
                    .font(.system(size: 42, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                
                Text("Thin Client")
                    .font(.title3)
                    .foregroundColor(.secondary)
                    .tracking(2)
            }
            
            // Description
            Text("Dein persönlicher AI-Assistant.\nVerbinde dich mit deinem OpenClaw Gateway.")
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            
            Spacer()
            
            // Setup Button
            Button(action: { showSetup = true }) {
                HStack {
                    Text("Einrichten")
                        .font(.headline)
                    Image(systemName: "arrow.right.circle.fill")
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding()
                .background(
                    LinearGradient(
                        colors: [.blue, .purple],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .cornerRadius(16)
            }
            .padding(.horizontal, 32)
            .padding(.bottom, 50)
        }
        .sheet(isPresented: $showSetup) {
            SetupView()
        }
    }
}

#Preview {
    WelcomeView()
}
