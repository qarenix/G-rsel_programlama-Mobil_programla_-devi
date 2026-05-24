package com.ensoz.varlikyonetimi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BRAND = Color.rgb(20, 103, 94);
    private static final int BRAND_DARK = Color.rgb(8, 68, 62);
    private static final int SCREEN = Color.rgb(246, 248, 247);
    private static final int SOFT_GREEN = Color.rgb(225, 242, 236);
    private static final int SOFT_BLUE = Color.rgb(225, 237, 249);
    private static final int SOFT_GOLD = Color.rgb(255, 241, 206);
    private static final int SOFT_RED = Color.rgb(249, 228, 226);
    private static final int TEXT = Color.rgb(31, 42, 48);
    private static final int MUTED = Color.rgb(103, 116, 124);

    private final String[] categories = {"Tümü", "Konut", "Arsa", "Elektronik", "Eşya", "Araç", "Nakit", "Diğer"};
    private final String[] statuses = {"Aktif", "Satıldı", "Bakımda", "Depoda"};
    private final String[] moneyTypes = {"Gider", "Alışveriş", "Yatırım"};

    private AssetStore assetStore;
    private FinanceStore financeStore;
    private List<Asset> assets = new ArrayList<>();
    private List<FinanceEntry> financeEntries = new ArrayList<>();
    private LinearLayout listContainer;
    private LinearLayout financeList;
    private TextView headerAssetTotalText;
    private TextView incomeText;
    private TextView expenseText;
    private TextView remainingText;
    private TextView assetTotalText;
    private TextView assetCountText;
    private EditText searchInput;
    private Spinner categoryFilter;
    private String selectedCategory = "Tümü";
    private String query = "";
    private double monthlyIncome = 0;
    private boolean financeExpanded = false;
    private boolean assetsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assetStore = new AssetStore(this);
        financeStore = new FinanceStore(this);
        assets = assetStore.load();
        financeEntries = financeStore.loadEntries();
        monthlyIncome = financeStore.loadIncome();
        setContentView(buildContent());
        render();
    }

    private View buildContent() {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(SCREEN);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(16), dp(16), dp(16), dp(86));
        frame.addView(main, new FrameLayout.LayoutParams(-1, -1));

        ScrollView pageScroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        pageScroll.addView(page);
        main.addView(pageScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout header = panel(BRAND, dp(18), dp(18));
        header.setBackground(gradient(BRAND_DARK, BRAND, dp(18)));
        header.setElevation(dp(5));
        page.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView title = text("Varlık ve Maaş Takibi", 24, Color.WHITE, Typeface.BOLD);
        header.addView(title);
        TextView subtitle = text("Gelir, gider ve varlıklarını tek ekranda gör", 14, Color.argb(225, 255, 255, 255), Typeface.NORMAL);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        header.addView(subtitle);

        View accent = new View(this);
        accent.setBackgroundColor(Color.rgb(255, 213, 105));
        LinearLayout.LayoutParams accentParams = new LinearLayout.LayoutParams(dp(74), dp(4));
        accentParams.setMargins(0, 0, 0, dp(14));
        header.addView(accent, accentParams);

        LinearLayout featureRow = new LinearLayout(this);
        featureRow.setOrientation(LinearLayout.HORIZONTAL);
        featureRow.setPadding(0, 0, 0, dp(10));
        header.addView(featureRow);
        featureRow.addView(headerBadge("Kolay Giriş"));
        featureRow.addView(headerBadge("Varlık + Maaş"));

        headerAssetTotalText = headerRow("Toplam Varlık", "0 TL");
        incomeText = headerRow("Aylık Gelir", "0 TL");
        expenseText = headerRow("Aylık Gider", "0 TL");
        remainingText = headerRow("Kalan Para", "0 TL");
        header.addView(headerAssetTotalText);
        header.addView(incomeText);
        header.addView(expenseText);
        header.addView(remainingText);

        LinearLayout moneyButtons = new LinearLayout(this);
        moneyButtons.setOrientation(LinearLayout.HORIZONTAL);
        moneyButtons.setPadding(0, dp(12), 0, 0);
        header.addView(moneyButtons);
        moneyButtons.addView(actionButton("Maaş Gir", Color.WHITE, BRAND_DARK, new View.OnClickListener() {
            @Override public void onClick(View v) { showIncomeDialog(); }
        }), new LinearLayout.LayoutParams(0, dp(46), 1));
        LinearLayout.LayoutParams moneyParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        moneyParams.leftMargin = dp(10);
        moneyButtons.addView(actionButton("Gider", Color.WHITE, BRAND_DARK, new View.OnClickListener() {
            @Override public void onClick(View v) { showFinanceDialog(null); }
        }), moneyParams);

        LinearLayout financePanel = panel(Color.WHITE, dp(16), dp(14));
        LinearLayout.LayoutParams financePanelParams = new LinearLayout.LayoutParams(-1, -2);
        financePanelParams.setMargins(0, dp(12), 0, 0);
        page.addView(financePanel, financePanelParams);
        financePanel.addView(sectionTitle("Para Nereye Gitti?"));
        financePanel.addView(thinLine());
        financeList = new LinearLayout(this);
        financeList.setOrientation(LinearLayout.VERTICAL);
        financePanel.addView(financeList);

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams summaryParams = new LinearLayout.LayoutParams(-1, -2);
        summaryParams.setMargins(0, dp(12), 0, 0);
        page.addView(summary, summaryParams);

        assetTotalText = smallMetric("Toplam Varlık", "0 TL");
        assetCountText = smallMetric("Kayıt Sayısı", "0");
        summary.addView(assetTotalText, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(0, -2, 1);
        countParams.leftMargin = dp(10);
        summary.addView(assetCountText, countParams);

        Button allAssetsButton = actionButton("Toplam Varlıkları Göster", BRAND_DARK, Color.WHITE, new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, SummaryActivity.class)); }
        });
        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(-1, dp(48));
        allParams.setMargins(0, dp(12), 0, 0);
        page.addView(allAssetsButton, allParams);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(-1, -2);
        controlsParams.setMargins(0, dp(12), 0, dp(10));
        page.addView(controls, controlsParams);

        searchInput = input("Ara: konut, arsa, elektronik...", "", InputType.TYPE_CLASS_TEXT);
        searchInput.setBackground(card(Color.WHITE, dp(14), Color.rgb(222, 229, 226)));
        searchInput.setPadding(dp(12), 0, dp(12), 0);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString().trim().toLowerCase(new Locale("tr", "TR"));
                renderAssets();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        controls.addView(searchInput, new LinearLayout.LayoutParams(0, dp(50), 1));

        categoryFilter = spinner(categories, "Tümü", false);
        categoryFilter.setBackground(card(Color.WHITE, dp(14), Color.rgb(222, 229, 226)));
        categoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
                renderAssets();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        LinearLayout.LayoutParams filterParams = new LinearLayout.LayoutParams(dp(118), dp(50));
        filterParams.leftMargin = dp(8);
        controls.addView(categoryFilter, filterParams);

        TextView listTitle = sectionTitle("Varlık Listesi");
        LinearLayout.LayoutParams listTitleParams = new LinearLayout.LayoutParams(-1, -2);
        listTitleParams.setMargins(0, dp(4), 0, dp(8));
        page.addView(listTitle, listTitleParams);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        page.addView(listContainer);

        Button addButton = actionButton("+ Yeni Varlık", BRAND_DARK, Color.WHITE, new View.OnClickListener() {
            @Override public void onClick(View v) { showAssetDialog(null); }
        });
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(dp(160), dp(56), Gravity.BOTTOM | Gravity.RIGHT);
        addParams.setMargins(0, 0, dp(18), dp(18));
        frame.addView(addButton, addParams);

        return frame;
    }

    private void render() {
        renderMoney();
        renderFinanceList();
        renderAssets();
    }

    private void renderMoney() {
        double expense = totalExpense();
        double assetTotal = totalAssetValue();
        headerAssetTotalText.setText("Toplam Varlık\n" + money(assetTotal));
        incomeText.setText("Aylık Gelir\n" + money(monthlyIncome));
        expenseText.setText("Aylık Gider\n" + money(expense));
        remainingText.setText("Kalan Para\n" + money(monthlyIncome - expense));
    }

    private void renderFinanceList() {
        financeList.removeAllViews();
        if (financeEntries.isEmpty()) {
            TextView empty = text("Henüz gider, alışveriş veya yatırım eklenmedi.", 14, MUTED, Typeface.NORMAL);
            empty.setPadding(0, dp(8), 0, 0);
            financeList.addView(empty);
            return;
        }

        int limit = financeExpanded ? financeEntries.size() : Math.min(3, financeEntries.size());
        for (int i = 0; i < limit; i++) {
            final FinanceEntry entry = financeEntries.get(i);
            TextView row = text(entry.type + "  |  " + money(entry.amount) + "\n" + entry.title + " - " + safe(entry.target), 14, TEXT, Typeface.BOLD);
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            row.setBackground(card(financeColor(entry.type), dp(12), 0));
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showFinanceDialog(entry); }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, dp(6), 0, 0);
            financeList.addView(row, params);
        }

        if (financeEntries.size() > 3) {
            Button toggle = actionButton(financeExpanded ? "Giderleri Kapat" : "Tüm Giderleri Göster", Color.rgb(238, 244, 242), BRAND_DARK, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    financeExpanded = !financeExpanded;
                    renderFinanceList();
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(44));
            params.setMargins(0, dp(8), 0, 0);
            financeList.addView(toggle, params);
        }
    }

    private void renderAssets() {
        if (listContainer == null) return;
        listContainer.removeAllViews();

        double total = totalAssetValue();
        assetTotalText.setText("Toplam Varlık\n" + money(total));
        assetCountText.setText("Kayıt Sayısı\n" + assets.size());

        List<Asset> filtered = filteredAssets();
        if (filtered.isEmpty()) {
            TextView empty = text("Kayıt bulunamadı.", 16, MUTED, Typeface.BOLD);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            listContainer.addView(empty);
            return;
        }

        int limit = assetsExpanded ? filtered.size() : Math.min(3, filtered.size());
        for (int i = 0; i < limit; i++) {
            Asset asset = filtered.get(i);
            listContainer.addView(assetCard(asset));
        }

        if (filtered.size() > 3) {
            Button toggle = actionButton(assetsExpanded ? "Varlıkları Kapat" : "Tüm Varlıkları Göster", Color.rgb(238, 244, 242), BRAND_DARK, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    assetsExpanded = !assetsExpanded;
                    renderAssets();
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(46));
            params.setMargins(0, dp(4), 0, dp(10));
            listContainer.addView(toggle, params);
        }
    }

    private List<Asset> filteredAssets() {
        List<Asset> filtered = new ArrayList<>();
        Locale tr = new Locale("tr", "TR");
        for (Asset asset : assets) {
            boolean categoryOk = "Tümü".equals(selectedCategory) || selectedCategory.equals(asset.category);
            String haystack = (safe(asset.name) + " " + safe(asset.category) + " " + safe(asset.location) + " " +
                    safe(asset.owner) + " " + safe(asset.serial) + " " + safe(asset.detail1) + " " +
                    safe(asset.detail2) + " " + safe(asset.detail3)).toLowerCase(tr);
            boolean queryOk = query.isEmpty() || haystack.contains(query);
            if (categoryOk && queryOk) filtered.add(asset);
        }
        return filtered;
    }

    private View assetCard(final Asset asset) {
        LinearLayout cardView = panel(Color.WHITE, dp(18), dp(14));
        cardView.setElevation(dp(2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        cardView.setLayoutParams(params);

        View topAccent = new View(this);
        topAccent.setBackgroundColor(typeTextColor(asset.category));
        LinearLayout.LayoutParams topAccentParams = new LinearLayout.LayoutParams(-1, dp(3));
        topAccentParams.setMargins(0, 0, 0, dp(12));
        cardView.addView(topAccent, topAccentParams);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        cardView.addView(top);

        TextView icon = typeIcon(asset.category);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        iconParams.rightMargin = dp(12);
        top.addView(icon, iconParams);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        top.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        left.addView(text(asset.name, 18, TEXT, Typeface.BOLD));
        TextView type = text(asset.category + " - " + asset.status, 13, MUTED, Typeface.NORMAL);
        type.setPadding(0, dp(3), 0, 0);
        left.addView(type);

        TextView value = text(money(asset.value), 16, BRAND_DARK, Typeface.BOLD);
        value.setGravity(Gravity.RIGHT);
        top.addView(value);

        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-1, -2);
        badgeParams.setMargins(0, dp(12), 0, 0);
        cardView.addView(badges, badgeParams);
        badges.addView(badge(asset.category, typeColor(asset.category), typeTextColor(asset.category)));
        badges.addView(badge(asset.status, statusColor(asset.status), TEXT));

        String info = detailSummary(asset);
        if (!info.trim().isEmpty()) {
            TextView details = text(info, 13, TEXT, Typeface.NORMAL);
            details.setPadding(0, dp(10), 0, 0);
            cardView.addView(details);
        }

        TextView bottom = text("Konum: " + safe(asset.location) + "    Sorumlu: " + safe(asset.owner), 13, MUTED, Typeface.NORMAL);
        bottom.setPadding(0, dp(8), 0, 0);
        cardView.addView(bottom);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAssetDialog(asset); }
        });
        return cardView;
    }

    private TextView typeIcon(String category) {
        String letter = "V";
        if ("Konut".equals(category)) letter = "K";
        if ("Arsa".equals(category)) letter = "A";
        if ("Elektronik".equals(category)) letter = "E";
        if ("Eşya".equals(category)) letter = "Ş";
        if ("Araç".equals(category)) letter = "O";
        if ("Nakit".equals(category)) letter = "N";

        TextView view = text(letter, 18, typeTextColor(category), Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setBackground(card(typeColor(category), dp(22), 0));
        return view;
    }

    private int typeColor(String category) {
        if ("Konut".equals(category)) return SOFT_GREEN;
        if ("Arsa".equals(category)) return SOFT_GOLD;
        if ("Elektronik".equals(category)) return SOFT_BLUE;
        if ("Eşya".equals(category)) return Color.rgb(235, 229, 249);
        if ("Araç".equals(category)) return SOFT_RED;
        if ("Nakit".equals(category)) return Color.rgb(224, 245, 226);
        return Color.rgb(233, 238, 241);
    }

    private int typeTextColor(String category) {
        if ("Arsa".equals(category)) return Color.rgb(125, 82, 8);
        if ("Elektronik".equals(category)) return Color.rgb(35, 75, 126);
        if ("Araç".equals(category)) return Color.rgb(138, 56, 47);
        return BRAND_DARK;
    }

    private int financeColor(String type) {
        if ("Yatırım".equals(type)) return SOFT_GREEN;
        if ("Alışveriş".equals(type)) return SOFT_GOLD;
        return SOFT_RED;
    }

    private int statusColor(String status) {
        if ("Aktif".equals(status)) return SOFT_GREEN;
        if ("Satıldı".equals(status)) return Color.rgb(232, 232, 232);
        if ("Bakımda".equals(status)) return SOFT_GOLD;
        return SOFT_BLUE;
    }

    private void showIncomeDialog() {
        final EditText incomeInput = moneyInput("Aylık gelir", moneyPlain(monthlyIncome));
        LinearLayout form = dialogForm();
        form.addView(incomeInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Aylık Gelir")
                .setView(form)
                .setNegativeButton("İptal", null)
                .setPositiveButton("Kaydet", null)
                .create();
        dialog.setOnShowListener(d -> {
            focusAndShowKeyboard(dialog, incomeInput);
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setTextColor(BRAND_DARK);
            save.setOnClickListener(v -> {
                monthlyIncome = parseMoney(incomeInput.getText().toString());
                financeStore.saveIncome(monthlyIncome);
                renderMoney();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showFinanceDialog(final FinanceEntry editing) {
        final boolean isNew = editing == null;
        final FinanceEntry working = isNew ? new FinanceEntry() : editing;
        LinearLayout form = dialogForm();
        final Spinner type = spinner(moneyTypes, safe(working.type).isEmpty() ? "Gider" : working.type, false);
        final Spinner assetSpinner = spinner(assetChoices(), assets.isEmpty() ? "Varlık yok" : assetChoice(assets.get(0)), false);
        final EditText title = input("Ne alındı / neye harcandı?", working.title, InputType.TYPE_CLASS_TEXT);
        final EditText target = input("Nereye gitti? Market, banka, yatırım vb.", working.target, InputType.TYPE_CLASS_TEXT);
        final EditText amount = moneyInput("Tutar", isNew ? "" : moneyPlain(working.amount));

        form.addView(label("Kayıt türü"));
        form.addView(type);
        form.addView(label("İlgili varlık"));
        form.addView(assetSpinner);
        form.addView(title);
        form.addView(target);
        form.addView(amount);

        assetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean firstSelection = true;
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSelection) {
                    firstSelection = false;
                    return;
                }
                if (!isNew) return;
                if (assets.isEmpty()) return;
                Asset selected = assets.get(Math.max(0, position));
                title.setText(selected.name);
                target.setText(selected.category + " - " + selected.location);
                if (amount.getText().toString().trim().isEmpty() || "0".equals(amount.getText().toString().trim())) {
                    amount.setText(moneyPlain(selected.value));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isNew ? "Gider Ekle" : "Gideri Düzenle")
                .setView(form)
                .setNegativeButton("İptal", null)
                .setNeutralButton(isNew ? null : "Sil", null)
                .setPositiveButton("Kaydet", null)
                .create();
        dialog.setOnShowListener(d -> {
            focusAndShowKeyboard(dialog, title);
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setTextColor(BRAND_DARK);
            save.setOnClickListener(v -> {
                if (title.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Açıklama yazılmalı", Toast.LENGTH_SHORT).show();
                    return;
                }
                working.type = type.getSelectedItem().toString();
                working.title = title.getText().toString().trim();
                working.target = target.getText().toString().trim();
                working.amount = parseMoney(amount.getText().toString());
                if (isNew) {
                    working.id = String.valueOf(System.currentTimeMillis());
                    working.createdAt = System.currentTimeMillis();
                    financeEntries.add(0, working);
                }
                syncInvestmentAsset(working);
                financeStore.saveEntries(financeEntries);
                assetStore.save(assets);
                render();
                dialog.dismiss();
            });

            Button delete = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (delete != null) {
                delete.setTextColor(Color.rgb(176, 55, 55));
                delete.setOnClickListener(v -> {
                    removeLinkedInvestmentAsset(editing);
                    financeEntries.remove(editing);
                    financeStore.saveEntries(financeEntries);
                    assetStore.save(assets);
                    render();
                    dialog.dismiss();
                });
            }
        });
        dialog.show();
    }

    private void showAssetDialog(final Asset editing) {
        final boolean isNew = editing == null;
        final Asset working = isNew ? new Asset() : editing;
        LinearLayout form = dialogForm();

        final Spinner category = spinner(categories, safe(working.category).isEmpty() ? "Konut" : working.category, true);
        final Spinner status = spinner(statuses, safe(working.status).isEmpty() ? "Aktif" : working.status, false);
        final EditText name = input("Varlık adı", working.name, InputType.TYPE_CLASS_TEXT);
        final EditText value = moneyInput("Değer", isNew ? "" : moneyPlain(working.value));
        final EditText location = input("Konum / İlçe", working.location, InputType.TYPE_CLASS_TEXT);
        final EditText owner = input("Sorumlu kişi", working.owner, InputType.TYPE_CLASS_TEXT);
        final EditText serial = input("Seri / plaka / tapu no", working.serial, InputType.TYPE_CLASS_TEXT);
        final EditText date = input("Alım tarihi", working.purchaseDate, InputType.TYPE_CLASS_TEXT);
        final EditText detail1 = input("", working.detail1, InputType.TYPE_CLASS_TEXT);
        final EditText detail2 = input("", working.detail2, InputType.TYPE_CLASS_TEXT);
        final EditText detail3 = input("", working.detail3, InputType.TYPE_CLASS_TEXT);
        final EditText detail4 = input("", working.detail4, InputType.TYPE_CLASS_TEXT);

        form.addView(label("Varlık türü"));
        form.addView(category);
        form.addView(label("Durum"));
        form.addView(status);
        form.addView(name);
        form.addView(value);
        form.addView(location);
        form.addView(owner);
        form.addView(serial);
        form.addView(date);
        form.addView(detail1);
        form.addView(detail2);
        form.addView(detail3);
        form.addView(detail4);

        Runnable updateHints = new Runnable() {
            @Override public void run() {
                String selected = category.getSelectedItem().toString();
                String[] hints = detailHints(selected);
                detail1.setHint(hints[0]);
                detail2.setHint(hints[1]);
                detail3.setHint(hints[2]);
                detail4.setHint(hints[3]);
                detail1.setInputType(isNumberHint(hints[0]) ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
                detail2.setInputType(isNumberHint(hints[1]) ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
            }
        };
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateHints.run(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        updateHints.run();

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(form);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isNew ? "Yeni Varlık" : "Varlığı Düzenle")
                .setView(scrollView)
                .setNegativeButton("İptal", null)
                .setNeutralButton(isNew ? null : "Sil", null)
                .setPositiveButton("Kaydet", null)
                .create();

        dialog.setOnShowListener(d -> {
            focusAndShowKeyboard(dialog, name);
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setTextColor(BRAND_DARK);
            save.setOnClickListener(v -> {
                if (name.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Varlık adı gerekli", Toast.LENGTH_SHORT).show();
                    return;
                }
                working.category = category.getSelectedItem().toString();
                working.status = status.getSelectedItem().toString();
                working.name = name.getText().toString().trim();
                working.value = parseMoney(value.getText().toString());
                working.location = location.getText().toString().trim();
                working.owner = owner.getText().toString().trim();
                working.serial = serial.getText().toString().trim();
                working.purchaseDate = date.getText().toString().trim();
                working.detail1 = detail1.getText().toString().trim();
                working.detail2 = detail2.getText().toString().trim();
                working.detail3 = detail3.getText().toString().trim();
                working.detail4 = detail4.getText().toString().trim();
                working.notes = "";
                if (isNew) {
                    working.id = String.valueOf(System.currentTimeMillis());
                    working.createdAt = System.currentTimeMillis();
                    assets.add(0, working);
                }
                assetStore.save(assets);
                renderAssets();
                dialog.dismiss();
            });

            Button delete = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (delete != null) {
                delete.setTextColor(Color.rgb(176, 55, 55));
                delete.setOnClickListener(v -> {
                    assets.remove(editing);
                    assetStore.save(assets);
                    renderAssets();
                    dialog.dismiss();
                });
            }
        });
        dialog.show();
    }

    private void showAllAssetsDialog() {
        LinearLayout form = dialogForm();
        double total = totalAssetValue();
        TextView totalText = text("Toplam eklenen varlık: " + assets.size() + "\nToplam değer: " + money(total), 17, BRAND_DARK, Typeface.BOLD);
        totalText.setPadding(0, 0, 0, dp(10));
        form.addView(totalText);

        if (assets.isEmpty()) {
            form.addView(text("Henüz varlık eklenmedi.", 14, MUTED, Typeface.NORMAL));
        } else {
            for (Asset asset : assets) {
                TextView row = text(asset.category + " - " + asset.name + "\n" + money(asset.value) + " | " + detailSummary(asset), 14, TEXT, Typeface.NORMAL);
                row.setPadding(0, dp(8), 0, dp(8));
                row.setBackground(card(Color.rgb(248, 250, 249), dp(10), Color.rgb(230, 235, 233)));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                params.setMargins(0, dp(6), 0, 0);
                form.addView(row, params);
            }
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(form);
        new AlertDialog.Builder(this)
                .setTitle("Toplam Varlıklar")
                .setView(scrollView)
                .setPositiveButton("Tamam", null)
                .show();
    }

    private String[] detailHints(String category) {
        if ("Konut".equals(category)) return new String[]{"Oda sayısı", "m²", "Kat bilgisi", "Tapu / kredi durumu"};
        if ("Arsa".equals(category)) return new String[]{"m²", "Ada / parsel", "İmar durumu", "Ek bilgi"};
        if ("Elektronik".equals(category)) return new String[]{"Marka", "Model", "Hafıza / özellik", "Garanti durumu"};
        if ("Eşya".equals(category)) return new String[]{"Eşya türü", "Adet", "Bulunduğu oda", "Durum notu"};
        if ("Araç".equals(category)) return new String[]{"Plaka", "Km", "Model yılı", "Sigorta / muayene"};
        if ("Nakit".equals(category)) return new String[]{"Para türü", "Banka / kasa", "Hesap adı", "Açıklama"};
        return new String[]{"Bilgi 1", "Bilgi 2", "Bilgi 3", "Bilgi 4"};
    }

    private String detailSummary(Asset asset) {
        String[] labels = detailHints(asset.category);
        StringBuilder builder = new StringBuilder();
        appendDetail(builder, labels[0], asset.detail1);
        appendDetail(builder, labels[1], asset.detail2);
        appendDetail(builder, labels[2], asset.detail3);
        appendDetail(builder, labels[3], asset.detail4);
        return builder.toString();
    }

    private void appendDetail(StringBuilder builder, String label, String value) {
        if (safe(value).trim().isEmpty()) return;
        if (builder.length() > 0) builder.append("  |  ");
        builder.append(label).append(": ").append(value);
    }

    private boolean isNumberHint(String hint) {
        return hint.contains("m²") || "Adet".equals(hint) || "Km".equals(hint) || hint.contains("yılı");
    }

    private double totalExpense() {
        double total = 0;
        for (FinanceEntry entry : financeEntries) {
            total += entry.amount;
        }
        return total;
    }

    private double totalAssetValue() {
        double total = 0;
        for (Asset asset : assets) total += asset.value;
        return total;
    }

    private String[] assetChoices() {
        if (assets.isEmpty()) return new String[]{"Varlık yok"};
        String[] choices = new String[assets.size()];
        for (int i = 0; i < assets.size(); i++) {
            choices[i] = assetChoice(assets.get(i));
        }
        return choices;
    }

    private String assetChoice(Asset asset) {
        return asset.name + " - " + asset.category + " - " + money(asset.value);
    }

    private void syncInvestmentAsset(FinanceEntry entry) {
        if (!"Yatırım".equals(entry.type)) {
            removeLinkedInvestmentAsset(entry);
            entry.assetId = "";
            return;
        }

        Asset linked = findAssetById(entry.assetId);
        if (linked == null) {
            linked = new Asset();
            linked.id = "yatirim-" + entry.id;
            linked.createdAt = System.currentTimeMillis();
            assets.add(0, linked);
            entry.assetId = linked.id;
        }

        linked.name = entry.title.trim().isEmpty() ? "Yatırım" : entry.title.trim();
        linked.category = "Nakit";
        linked.status = "Aktif";
        linked.location = safe(entry.target).trim().isEmpty() ? "Yatırım hesabı" : entry.target.trim();
        linked.owner = "Ben";
        linked.serial = "YTR-" + entry.id;
        linked.purchaseDate = "";
        linked.value = entry.amount;
        linked.detail1 = "Yatırım";
        linked.detail2 = linked.location;
        linked.detail3 = money(entry.amount);
        linked.detail4 = "Gider kaydından otomatik eklendi";
        linked.notes = "";
    }

    private void removeLinkedInvestmentAsset(FinanceEntry entry) {
        if (entry == null || safe(entry.assetId).isEmpty()) return;
        Asset linked = findAssetById(entry.assetId);
        if (linked != null) {
            assets.remove(linked);
        }
    }

    private Asset findAssetById(String id) {
        if (safe(id).isEmpty()) return null;
        for (Asset asset : assets) {
            if (id.equals(asset.id)) return asset;
        }
        return null;
    }

    private TextView headerRow(String label, String value) {
        TextView view = text(label + "\n" + value, 16, Color.WHITE, Typeface.BOLD);
        view.setPadding(dp(14), dp(11), dp(14), dp(11));
        view.setBackground(card(Color.argb(36, 255, 255, 255), dp(12), Color.argb(70, 255, 255, 255)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(8));
        view.setLayoutParams(params);
        return view;
    }

    private TextView smallMetric(String label, String value) {
        TextView view = text(label + "\n" + value, 15, TEXT, Typeface.BOLD);
        view.setPadding(dp(14), dp(12), dp(14), dp(12));
        view.setBackground(card(Color.WHITE, dp(16), Color.rgb(226, 232, 230)));
        return view;
    }

    private TextView sectionTitle(String value) {
        TextView view = text(value, 17, TEXT, Typeface.BOLD);
        view.setPadding(0, 0, 0, dp(4));
        return view;
    }

    private TextView headerBadge(String value) {
        TextView view = text(value, 11, Color.WHITE, Typeface.BOLD);
        view.setPadding(dp(8), dp(5), dp(8), dp(5));
        view.setSingleLine(true);
        view.setBackground(card(Color.argb(42, 255, 255, 255), dp(16), Color.argb(60, 255, 255, 255)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(0, 0, dp(7), 0);
        view.setLayoutParams(params);
        return view;
    }

    private View thinLine() {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(231, 237, 234));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(1));
        params.setMargins(0, dp(4), 0, dp(6));
        line.setLayoutParams(params);
        return line;
    }

    private TextView badge(String value, int fill, int color) {
        TextView view = text(value, 12, color, Typeface.BOLD);
        view.setPadding(dp(10), dp(5), dp(10), dp(5));
        view.setSingleLine(true);
        view.setBackground(card(fill, dp(18), 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(0, 0, dp(8), 0);
        view.setLayoutParams(params);
        return view;
    }

    private Button actionButton(String title, int fill, int textColor, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextColor(textColor);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setBackground(card(fill, dp(14), 0));
        button.setElevation(dp(2));
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout panel(int color, int radius, int padding) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackground(card(color, radius, color == Color.WHITE ? Color.rgb(226, 232, 230) : 0));
        layout.setElevation(color == Color.WHITE ? dp(1) : dp(3));
        return layout;
    }

    private LinearLayout dialogForm() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), 0);
        return form;
    }

    private EditText input(String hint, String value, int type) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(safe(value));
        if ((type & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
            input.setInputType(type | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            input.setTextLocale(new Locale("tr", "TR"));
            input.setTextDirection(View.TEXT_DIRECTION_LOCALE);
            input.setPrivateImeOptions("com.google.android.inputmethod.latin.locale=tr");
        } else {
            input.setInputType(type);
        }
        input.setTextColor(TEXT);
        input.setHintTextColor(MUTED);
        input.setTextSize(15);
        input.setSingleLine(true);
        input.setPadding(0, dp(8), 0, dp(8));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        return input;
    }

    private EditText moneyInput(String hint, String value) {
        final EditText input = input(hint, value, InputType.TYPE_CLASS_NUMBER);
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        input.addTextChangedListener(new TextWatcher() {
            private boolean editing;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;
                String formatted = moneyPlain(parseMoney(s.toString()));
                input.setText(formatted);
                input.setSelection(input.getText().length());
                editing = false;
            }
        });
        return input;
    }

    private void focusAndShowKeyboard(AlertDialog dialog, final EditText input) {
        input.requestFocus();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        input.postDelayed(new Runnable() {
            @Override public void run() {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);
    }

    private TextView label(String value) {
        TextView view = text(value, 12, MUTED, Typeface.BOLD);
        view.setPadding(0, dp(12), 0, 0);
        return view;
    }

    private Spinner spinner(String[] values, String selected, boolean skipAll) {
        List<String> actual = new ArrayList<>();
        for (String value : values) {
            if (skipAll && "Tümü".equals(value)) continue;
            actual.add(value);
        }
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, actual);
        spinner.setAdapter(adapter);
        int index = actual.indexOf(selected);
        spinner.setSelection(Math.max(0, index));
        return spinner;
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(safe(value));
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private GradientDrawable card(int fill, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (stroke != 0) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private GradientDrawable gradient(int start, int end, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private String money(double value) {
        return moneyPlain(value) + " TL";
    }

    private String moneyPlain(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("tr", "TR"));
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,###", symbols);
        return format.format(Math.max(0, value));
    }

    private double parseMoney(String raw) {
        try {
            String clean = safe(raw).replace(".", "").replace(",", "").replace(" ", "");
            if (clean.isEmpty()) return 0;
            return Double.parseDouble(clean);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
