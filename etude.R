library(ggplot2)
library(dplyr)

# Lire le fichier CSV
data <- read.csv("U:/qualiteDev/Projets/scaling_results2.csv")

# --- STRONG SCALING ---
strong_data <- subset(data, testType == "strong")

# Calcul du speedup : référence = temps à 1 thread pour chaque nTot
strong_data <- strong_data %>%
  group_by(nTot) %>%
  mutate(speedup = median_ms[nbP == 1] / median_ms)

ggplot(strong_data, aes(x = nbP, y = speedup, color = factor(nTot))) +
  geom_line() +
  geom_point() +
  geom_line(aes(x = nbP, y = nbP), color = "black", linetype = "dashed") +  # courbe idéale
  labs(
    title = "Strong Scaling (Speedup)",
    x = "Nombre de threads",
    y = "Speedup",
    color = "nTot"
  ) +
  theme_minimal()

# --- WEAK SCALING ---

weak_data <- subset(data, testType == "weak")

# Calcul de l'efficiency (charge ajustée)
weak_data <- weak_data %>%
  mutate(efficiency = (median_ms[nbP == 1] / median_ms) * (nTot / nTot[nbP == 1]))

ggplot(weak_data, aes(x = nbP, y = efficiency)) +
  geom_line(color = "blue") +
  geom_point(color = "darkblue") +
  geom_hline(yintercept = 1, linetype = "dashed", color = "black") +  # ligne idéale
  labs(
    title = "Weak Scaling (Efficiency)",
    x = "Nombre de threads",
    y = "Efficiency"
  ) +
  theme_minimal()

