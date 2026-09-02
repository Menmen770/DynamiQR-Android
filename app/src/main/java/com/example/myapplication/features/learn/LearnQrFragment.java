package com.example.myapplication.features.learn;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.core.base.BaseFragment;
import com.example.myapplication.databinding.FragmentLearnQrBinding;
import com.example.myapplication.databinding.ItemLearnCheckRowBinding;
import com.example.myapplication.databinding.ItemLearnDiffCardBinding;
import com.example.myapplication.databinding.ItemLearnTimelineStepBinding;
import com.example.myapplication.databinding.ItemLearnUseCaseBinding;

public class LearnQrFragment extends BaseFragment<FragmentLearnQrBinding> {

    @Override
    protected FragmentLearnQrBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLearnQrBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.header.setTitle(getString(R.string.learn_page_title));
        binding.header.setSubtitle(getString(R.string.learn_page_subtitle));

        setupStatCards();
        setupSectionHeads();
        setupUseCases();
        setupWorkflow();
        setupDifferentiator();
        setupCompareCards();
        setupCheckList(binding.benefitsContainer, R.array.learn_benefits);
        setupCheckList(binding.tipsContainer, R.array.learn_tips);

        View.OnClickListener goCreate = v -> navigateToGenerator();
        binding.btnCreateHero.setOnClickListener(goCreate);
        binding.btnCreateSummary.setOnClickListener(goCreate);
    }

    private void navigateToGenerator() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToTab(R.id.nav_generator);
        }
    }

    private void setupStatCards() {
        binding.statFriction.statLabel.setText(R.string.learn_stat_friction_label);
        binding.statFriction.statStrong.setText(R.string.learn_stat_friction_strong);
        binding.statFriction.statText.setText(R.string.learn_stat_friction_text);

        binding.statFlex.statLabel.setText(R.string.learn_stat_flex_label);
        binding.statFlex.statStrong.setText(R.string.learn_stat_flex_strong);
        binding.statFlex.statText.setText(R.string.learn_stat_flex_text);
    }

    private void setupSectionHeads() {
        binding.useCasesHead.sectionKicker.setText(R.string.learn_use_cases_kicker);
        binding.useCasesHead.sectionTitle.setText(R.string.learn_use_cases_title);
        binding.useCasesHead.sectionSubtitle.setText(R.string.learn_use_cases_subtitle);

        binding.workflowHead.sectionKicker.setText(R.string.learn_workflow_kicker);
        binding.workflowHead.sectionTitle.setText(R.string.learn_workflow_title);
        binding.workflowHead.sectionSubtitle.setText(R.string.learn_workflow_subtitle);
    }

    private void setupUseCases() {
        int[][] data = {
                {R.string.learn_use_site_title, R.string.learn_use_site_text},
                {R.string.learn_use_wifi_title, R.string.learn_use_wifi_text},
                {R.string.learn_use_pdf_title, R.string.learn_use_pdf_text},
                {R.string.learn_use_leads_title, R.string.learn_use_leads_text}
        };
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int[] pair : data) {
            ItemLearnUseCaseBinding item = ItemLearnUseCaseBinding.inflate(inflater, binding.useCasesContainer, false);
            item.useCaseTitle.setText(pair[0]);
            item.useCaseText.setText(pair[1]);
            binding.useCasesContainer.addView(item.getRoot());
        }
    }

    private void setupWorkflow() {
        int[][] steps = {
                {R.string.learn_step_goal_title, R.string.learn_step_goal_text},
                {R.string.learn_step_design_title, R.string.learn_step_design_text},
                {R.string.learn_step_publish_title, R.string.learn_step_publish_text}
        };
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < steps.length; i++) {
            ItemLearnTimelineStepBinding step = ItemLearnTimelineStepBinding.inflate(inflater, binding.workflowContainer, false);
            step.stepPill.setText("שלב " + (i + 1));
            step.stepTitle.setText(steps[i][0]);
            step.stepText.setText(steps[i][1]);
            if (i == steps.length - 1) {
                step.stepLine.setVisibility(View.GONE);
            }
            binding.workflowContainer.addView(step.getRoot());
        }
    }

    private void setupDifferentiator() {
        String[] titles = getResources().getStringArray(R.array.learn_diff_titles);
        String[] texts = getResources().getStringArray(R.array.learn_diff_texts);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < titles.length; i++) {
            ItemLearnDiffCardBinding card = ItemLearnDiffCardBinding.inflate(inflater, binding.diffContainer, false);
            card.diffTitle.setText(titles[i]);
            card.diffText.setText(texts[i]);
            binding.diffContainer.addView(card.getRoot());
        }
    }

    private void setupCompareCards() {
        binding.staticCompareCard.compareAccent.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.sub_text));
        binding.staticCompareCard.compareBadge.setText(R.string.learn_static_badge);
        binding.staticCompareCard.compareTitle.setText(R.string.learn_static_title);
        binding.staticCompareCard.compareText.setText(R.string.learn_static_text);
        addCompareItem(binding.staticCompareCard.compareItemsContainer, R.string.learn_static_item1);
        addCompareItem(binding.staticCompareCard.compareItemsContainer, R.string.learn_static_item2);

        binding.dynamicCompareCard.compareAccent.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.primary));
        binding.dynamicCompareCard.compareBadge.setText(R.string.learn_dynamic_badge);
        binding.dynamicCompareCard.compareTitle.setText(R.string.learn_dynamic_title);
        binding.dynamicCompareCard.compareText.setText(R.string.learn_dynamic_text);
        addCompareItem(binding.dynamicCompareCard.compareItemsContainer, R.string.learn_dynamic_item1);
        addCompareItem(binding.dynamicCompareCard.compareItemsContainer, R.string.learn_dynamic_item2);
    }

    private void addCompareItem(LinearLayout container, int textRes) {
        ItemLearnCheckRowBinding row = ItemLearnCheckRowBinding.inflate(
                LayoutInflater.from(requireContext()), container, false);
        row.checkText.setText(textRes);
        container.addView(row.getRoot());
    }

    private void setupCheckList(LinearLayout container, int arrayRes) {
        String[] items = getResources().getStringArray(arrayRes);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String item : items) {
            ItemLearnCheckRowBinding row = ItemLearnCheckRowBinding.inflate(inflater, container, false);
            row.checkText.setText(item);
            container.addView(row.getRoot());
        }
    }
}
