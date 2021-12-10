import os
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np

directory = "results/"
full_directory = os.fsencode(os.getcwd() + "/" + directory)

dfs = []
filenames = []

for filename in os.listdir(full_directory):
    name = filename.decode()
    if name[-4:] == ".csv":
        filenames.append(os.path.splitext(name)[0])
        df = pd.read_csv(directory + name)
        dfs.append(df)

for i in range(len(dfs)):
    print(filenames[i], "Mean Monetary Value:",
          np.around(np.mean(dfs[i].iloc[:, [8]])[0] * 100, 2))

fig, axes = plt.subplots(2, 4, figsize=(15, 8), sharey=True, sharex=True)
for i in range(len(dfs)):
    ax = axes.flatten()[i]
    ax.set_title(filenames[i])
    ax.grid()
    #     sns.scatterplot(data=dfs[i], x="Deliveries Available", y="Delivery Completion Ratio", ax=ax, alpha=0.4)
    sns.scatterplot(data=dfs[i], x="Deliveries Available", y="Monetary Value Ratio", ax=ax, alpha=0.4)
fig.suptitle("Monetary Value Ratio Against Deliveries Available for Varying Heuristic Combinations")
plt.savefig("visuals/MonetaryValueFig.png", bbox_inches="tight")

fig, axes = plt.subplots(2, 4, figsize=(15, 8), sharey=True, sharex=True)
for i in range(len(dfs)):
    ax = axes.flatten()[i]
    df_full_completion = dfs[i][dfs[i]["Monetary Value Ratio"] >= 1]
    print(filenames[i], "Mean Moves Remaining:",
          np.around(np.mean(dfs[i].iloc[:, [9]])[0] * 100, 2),
          "|",
          "Days with full completion:",
          len(df_full_completion))

    ax.set_title(filenames[i])
    ax.grid()
    sns.scatterplot(data=df_full_completion, x="Deliveries Available", y="Moves Remaining", ax=ax, alpha=0.4)
fig.suptitle("Monetary Value Ratio Against Deliveries Available for Varying Heuristic Combinations")
plt.savefig("visuals/MovesRemainingFig.png", bbox_inches="tight")
